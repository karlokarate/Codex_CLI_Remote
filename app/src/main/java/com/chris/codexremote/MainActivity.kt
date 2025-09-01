package com.chris.codexremote

import android.content.Context
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.unit.dp
import androidx.datastore.preferences.core.*
import androidx.datastore.preferences.preferencesDataStore
import de.m3usuite.remote.R
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.launch
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import okhttp3.logging.HttpLoggingInterceptor
import net.schmizz.sshj.SSHClient
import net.schmizz.sshj.transport.verification.PromiscuousVerifier
import timber.log.Timber
import java.net.DatagramPacket
import java.net.DatagramSocket
import java.net.InetAddress

// DataStore
private val Context.dataStore by preferencesDataStore("m3usuite_remote")

// Keys
private object Prefs {
    val SSH_HOST = stringPreferencesKey("ssh_host")
    val SSH_USER = stringPreferencesKey("ssh_user")
    val SSH_PASS = stringPreferencesKey("ssh_pass") // TODO: später verschlüsseln
    val PC_MAC   = stringPreferencesKey("pc_mac")
    val WOL_BCAST = stringPreferencesKey("wol_bcast")
    val WOL_PORT  = intPreferencesKey("wol_port")
    val FB_HOST   = stringPreferencesKey("fb_host")
    val FB_USER   = stringPreferencesKey("fb_user")
    val FB_PASS   = stringPreferencesKey("fb_pass") // TODO: später verschlüsseln
}

class MainActivity : ComponentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        // Timber initialisieren (einfach)
        Timber.plant(Timber.DebugTree())

        setContent {
            MaterialTheme {
                AppScaffold()
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun AppScaffold() {
    val tabs = listOf("Setup", "Terminal")
    var selectedTab by remember { mutableStateOf(0) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(stringResource(R.string.app_name)) }
            )
        },
        bottomBar = {
            NavigationBar {
                tabs.forEachIndexed { i, title ->
                    NavigationBarItem(
                        selected = selectedTab == i,
                        onClick = { selectedTab = i },
                        icon = { },
                        label = { Text(title) }
                    )
                }
            }
        }
    ) { padding ->
        Box(Modifier.padding(padding)) {
            when (selectedTab) {
                0 -> SetupScreen()
                1 -> TerminalScreen()
            }
        }
    }
}

@Composable
private fun SetupScreen() {
    val ctx = LocalContext.current
    val scope = rememberCoroutineScope()
    // Explizite Default-Werte statt heterogenem mapOf (verursachte Typprobleme)
    val defaultSshHost = remember { "192.168.178.20" }
    val defaultSshUser = remember { "user" }
    val defaultPcMac = remember { "00:11:22:33:44:55" }
    val defaultWolBcast = remember { "192.168.178.255" }
    val defaultWolPort = remember { 9 }
    val defaultFbHost = remember { "http://192.168.178.1:49000" }
    val defaultFbUser = remember { "fritzuser" }

    // State laden
    val settingsFlow = ctx.dataStore.data.map { prefs ->
        SetupState(
            sshHost = prefs[Prefs.SSH_HOST] ?: defaultSshHost,
            sshUser = prefs[Prefs.SSH_USER] ?: defaultSshUser,
            sshPass = prefs[Prefs.SSH_PASS] ?: "",
            pcMac   = prefs[Prefs.PC_MAC]   ?: defaultPcMac,
            wolBcast= prefs[Prefs.WOL_BCAST]?: defaultWolBcast,
            wolPort = prefs[Prefs.WOL_PORT] ?: defaultWolPort,
            fbHost  = prefs[Prefs.FB_HOST]  ?: defaultFbHost,
            fbUser  = prefs[Prefs.FB_USER]  ?: defaultFbUser,
            fbPass  = prefs[Prefs.FB_PASS]  ?: ""
        )
    }

    val state by settingsFlow.collectAsState(initial = SetupState())

    // Editierbarer State
    var sshHost by remember { mutableStateOf(state.sshHost) }
    var sshUser by remember { mutableStateOf(state.sshUser) }
    var sshPass by remember { mutableStateOf(state.sshPass) }
    var pcMac   by remember { mutableStateOf(state.pcMac) }
    var wolB    by remember { mutableStateOf(state.wolBcast) }
    var wolPort by remember { mutableStateOf(state.wolPort) }
    var fbHost  by remember { mutableStateOf(state.fbHost) }
    var fbUser  by remember { mutableStateOf(state.fbUser) }
    var fbPass  by remember { mutableStateOf(state.fbPass) }

    val scroll = rememberScrollState()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(scroll)
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        Text("Remote‑Setup", style = MaterialTheme.typography.titleLarge)

        // SSH
        OutlinedTextField(sshHost, { sshHost = it }, label = { Text("SSH Host / IP") }, modifier = Modifier.fillMaxWidth())
        OutlinedTextField(sshUser, { sshUser = it }, label = { Text("SSH Benutzer") }, modifier = Modifier.fillMaxWidth())
        OutlinedTextField(sshPass, { sshPass = it }, label = { Text("SSH Passwort") },
            visualTransformation = PasswordVisualTransformation(), modifier = Modifier.fillMaxWidth())

        // WOL
        OutlinedTextField(pcMac, { pcMac = it }, label = { Text("PC MAC (WOL)") }, modifier = Modifier.fillMaxWidth())
        OutlinedTextField(wolB, { wolB = it }, label = { Text("Broadcast IP (WOL)") }, modifier = Modifier.fillMaxWidth())
        OutlinedTextField(wolPort.toString(), {
            wolPort = it.toIntOrNull() ?: 9
        }, label = { Text("WOL Port") }, modifier = Modifier.fillMaxWidth())

        // Fritz!Box TR-064
        OutlinedTextField(fbHost, { fbHost = it }, label = { Text("Fritz!Box TR‑064 Base (z. B. http://192.168.178.1:49000)") }, modifier = Modifier.fillMaxWidth())
        OutlinedTextField(fbUser, { fbUser = it }, label = { Text("Fritz!Box Benutzer") }, modifier = Modifier.fillMaxWidth())
        OutlinedTextField(fbPass, { fbPass = it }, label = { Text("Fritz!Box Passwort") },
            visualTransformation = PasswordVisualTransformation(), modifier = Modifier.fillMaxWidth())

        Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
            Button(onClick = {
                scope.launch {
                    savePrefs(ctx, sshHost, sshUser, sshPass, pcMac, wolB, wolPort, fbHost, fbUser, fbPass)
                    Timber.i("Gespeichert.")
                }
            }) { Text("Speichern") }

            Button(onClick = {
                scope.launch {
                    try {
                        sendWakeOnLan(pcMac, wolB, wolPort)
                        Timber.i("WOL gesendet an $pcMac via $wolB:$wolPort")
                    } catch (t: Throwable) {
                        Timber.e(t, "WOL fehlgeschlagen")
                    }
                }
            }) { Text("PC aufwecken (WOL)") }
        }

        Button(onClick = {
            scope.launch {
                // TR-064 Wake (Digest-Auth TODO – siehe Kommentar in Funktion)
                try {
                    val ok = fritzWakeOnLanByMac(fbBaseUrl = fbHost, username = fbUser, password = fbPass, mac = pcMac)
                    Timber.i("TR‑064 Wake result: $ok")
                } catch (t: Throwable) {
                    Timber.e(t, "TR‑064 Wake fehlgeschlagen")
                }
            }
        }) { Text("PC aufwecken (Fritz!Box TR‑064)") }
    }
}

@Composable
private fun TerminalScreen() {
    val ctx = LocalContext.current
    val scope = rememberCoroutineScope()
    var out by remember { mutableStateOf("") }
    var cmd by remember { mutableStateOf("wsl.exe -l -v") } // Beispiel: Windows-SSH -> prüft WSL

    val settingsFlow = ctx.dataStore.data.map { prefs ->
        Triple(
            prefs[Prefs.SSH_HOST] ?: "",
            prefs[Prefs.SSH_USER] ?: "",
            prefs[Prefs.SSH_PASS] ?: ""
        )
    }
    // Korrekte Nutzung: erst State sammeln, dann Triple entpacken
    val credsState by settingsFlow.collectAsState(initial = Triple("", "", ""))
    val (host, user, pass) = credsState

    Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
        Text("Terminal (SSH‑Einzelbefehl)", style = MaterialTheme.typography.titleLarge)
        Text("Host: $host – User: $user")

        OutlinedTextField(cmd, { cmd = it }, label = { Text("Befehl") }, modifier = Modifier.fillMaxWidth())

        Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
            Button(onClick = {
                scope.launch {
                    out += "\n$ $cmd\n"
                    val res = runCatching { sshExec(host, user, pass, cmd) }.getOrElse {
                        out += "[Fehler] ${it.message}\n"
                        Timber.e(it)
                        return@launch
                    }
                    out += res + "\n"
                }
            }) { Text("Ausführen") }

            Button(onClick = { out = "" }) { Text("Clear") }
        }

        HorizontalDivider()
        Text(out)
    }
}

// ---------- Persistenz ----------
private suspend fun savePrefs(
    ctx: Context,
    sshHost: String, sshUser: String, sshPass: String,
    pcMac: String, wolBcast: String, wolPort: Int,
    fbHost: String, fbUser: String, fbPass: String
) {
    ctx.dataStore.edit { p ->
        p[Prefs.SSH_HOST] = sshHost
        p[Prefs.SSH_USER] = sshUser
        p[Prefs.SSH_PASS] = sshPass
        p[Prefs.PC_MAC]   = pcMac
        p[Prefs.WOL_BCAST]= wolBcast
        p[Prefs.WOL_PORT] = wolPort
        p[Prefs.FB_HOST]  = fbHost
        p[Prefs.FB_USER]  = fbUser
        p[Prefs.FB_PASS]  = fbPass
    }
}

private data class SetupState(
    val sshHost: String = "",
    val sshUser: String = "",
    val sshPass: String = "",
    val pcMac: String = "",
    val wolBcast: String = "",
    val wolPort: Int = 9,
    val fbHost: String = "",
    val fbUser: String = "",
    val fbPass: String = ""
)

// ---------- WOL ----------
@Throws(Exception::class)
private fun sendWakeOnLan(mac: String, broadcastIp: String = "255.255.255.255", port: Int = 9) {
    val cleanMac = mac.replace("[-:]".toRegex(), "")
    require(cleanMac.length == 12) { "Ungültige MAC: $mac" }

    val macBytes = ByteArray(6) { i ->
        Integer.parseInt(cleanMac.substring(i * 2, i * 2 + 2), 16).toByte()
    }

    // Magic packet: 6x 0xFF + 16x MAC
    val bytes = ByteArray(6 + 16 * 6)
    for (i in 0 until 6) bytes[i] = 0xFF.toByte()
    for (i in 6 until bytes.size step macBytes.size) {
        System.arraycopy(macBytes, 0, bytes, i, macBytes.size)
    }

    DatagramSocket().use { socket ->
        socket.broadcast = true
        val address = InetAddress.getByName(broadcastIp)
        val packet = DatagramPacket(bytes, bytes.size, address, port)
        socket.send(packet)
    }
}

// ---------- TR‑064 (Fritz!Box) ----------
/**
 * Ruft auf dem TR‑064 Hosts‑Service die AVM‑Erweiterung `X_AVM-DE_WakeOnLANByMACAddress` auf.
 *
 * WICHTIG:
 *  - Viele Fritz!Boxen nutzen **HTTP Digest Auth** für TR‑064. OkHttp unterstützt Digest nicht out-of-the-box.
 *    Für echte Produktion bitte mit einem Digest‑Authenticator ergänzen oder HTTPS + Session‑Login verwenden.
 *  - Die Action ist im AVM‑Dokument "TR‑064 – First Steps" gelistet (Kapitel "Service Hosts"). :contentReference[oaicite:23]{index=23}
 */
private fun fritzWakeOnLanByMac(
    fbBaseUrl: String,
    username: String,
    password: String,
    mac: String
): Boolean {
    // SOAP Endpunkt für Hosts-Service (üblich: /upnp/control/hosts)
    val controlUrl = fbBaseUrl.trimEnd('/') + "/upnp/control/hosts"
    val serviceType = "urn:dslforum-org:service:Hosts:1" // Service-Typ
    val action = "X_AVM-DE_WakeOnLANByMACAddress"

    val envelope = """
        <?xml version="1.0" encoding="utf-8"?>
        <s:Envelope xmlns:s="http://schemas.xmlsoap.org/soap/envelope/" s:encodingStyle="http://schemas.xmlsoap.org/soap/encoding/">
          <s:Body>
            <u:$action xmlns:u="$serviceType">
              <NewMACAddress>$mac</NewMACAddress>
            </u:$action>
          </s:Body>
        </s:Envelope>
    """.trimIndent()

    val logging = HttpLoggingInterceptor().apply {
        level = HttpLoggingInterceptor.Level.BASIC
    }
    val client = OkHttpClient.Builder()
        .addInterceptor(logging)
        // TODO: Digest-Auth-Interceptor ergänzen, falls FB Digest verlangt.
        .build()

    val body = envelope.toRequestBody("text/xml; charset=utf-8".toMediaType())
    val req = Request.Builder()
        .url(controlUrl)
        .addHeader("Content-Type", "text/xml; charset=\"utf-8\"")
        .addHeader("SOAPACTION", "\"$serviceType#$action\"")
        // Achtung: Basic Auth funktioniert nicht auf allen Boxen; häufig wird Digest benötigt.
        .addHeader("Authorization", okhttp3.Credentials.basic(username, password))
        .post(body)
        .build()

    client.newCall(req).execute().use { resp ->
        val ok = resp.isSuccessful
        Timber.d("TR‑064 status=${resp.code} ok=$ok")
        return ok
    }
}

// ---------- SSH (Einzelbefehl) ----------
private fun sshExec(host: String, user: String, pass: String, command: String): String {
    require(host.isNotBlank()) { "SSH Host fehlt" }
    val ssh = SSHClient().apply {
        // TODO: In Produktion Host Keys verifizieren!
        addHostKeyVerifier(PromiscuousVerifier())
        connectTimeout = 10_000
        timeout = 30_000
    }
    return runCatching {
        ssh.connect(host)
        ssh.authPassword(user, pass)
        ssh.startSession().use { session ->
            val cmd = session.exec(command)
            val out = cmd.inputStream.bufferedReader().readText()
            cmd.join()
            out.ifBlank { "[kein Output]" }
        }
    }.onFailure {
        try { ssh.disconnect() } catch (_: Throwable) {}
    }.getOrThrow().also {
        ssh.disconnect()
    }
}
