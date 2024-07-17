package robok.dev.diagnostic.logic

import kotlinx.coroutines.*

class DiagnosticHandler(private val diagnosticListener: DiagnosticListener) {

    private var diagnosticJob: Job? = null

    fun init() {
        // Inicia o delay de 5 segundos para verificar diagnósticos
        startCheck()
    }

    private fun startCheck() {
        diagnosticJob?.cancel()
        diagnosticJob = CoroutineScope(Dispatchers.Main).launch {
            delay(5000L)
            diagnosticListener.onDiagnosticStatusReceive(false)
        }
    }

    fun onDiagnosticReceived(line: Int, positionStart: Int, positionEnd: Int, msg: String) {
        diagnosticJob?.cancel()
        diagnosticListener.onDiagnosticReceive(line, positionStart, positionEnd, msg)
    }
}