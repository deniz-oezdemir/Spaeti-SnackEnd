package ecommerce.infrastructure.logging

import org.aspectj.lang.ProceedingJoinPoint
import org.aspectj.lang.annotation.Around
import org.aspectj.lang.annotation.Aspect
import org.slf4j.LoggerFactory
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty
import org.springframework.stereotype.Component
import java.util.concurrent.TimeUnit

@Aspect
@Component
@ConditionalOnProperty(prefix = "logging.tracing.service", name = ["enabled"], havingValue = "true")
class ServiceLoggingAspect {
    private val log = LoggerFactory.getLogger(ServiceLoggingAspect::class.java)
    // Logs every public method in your service package
    @Around("within(ecommerce..service..*)")
    fun aroundService(pjp: ProceedingJoinPoint): Any? {
        val sig = "${pjp.signature.declaringType.simpleName}.${pjp.signature.name}"
        if (log.isDebugEnabled) log.debug("svc_start sig={} args=[{}]", sig, safeArgs(pjp.args))

        val started = System.nanoTime()
        return try {
            val result = pjp.proceed()
            val took = TimeUnit.NANOSECONDS.toMillis(System.nanoTime() - started)
            if (log.isDebugEnabled) log.debug("svc_done  sig={} took={}ms", sig, took)
            result
        } catch (ex: Exception) {
            val took = TimeUnit.NANOSECONDS.toMillis(System.nanoTime() - started)
            log.warn("svc_fail  sig={} took={}ms msg={}", sig, took, ex.message)
            throw ex
        }
    }

    private fun safeArgs(args: Array<out Any?>): String =
        args.joinToString(",") { a ->
            when (a) {
                null -> "null"
                is String -> a.replace(Regex("(^.).*(@.*$)"), "$1***$2")
                else -> a.toString().let { if (it.length > 200) it.take(200) + "…" else it }
            }
        }
}
