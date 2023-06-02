package daily_planner.stubs.utils

import com.google.protobuf.Timestamp
import java.time.Instant

class DateUtil {
    companion object {
        fun convertProtoToInstance(timestamp: Timestamp) : Instant? {
            if ( timestamp.seconds == 0L || timestamp.nanos == 0) {
                return null
            }
            return Instant.ofEpochSecond(timestamp.seconds, timestamp.nanos.toLong())
        }

        fun convertToProtoTimestamp(instance: Instant?) : Timestamp? {
             return instance?.let {
                 Timestamp.newBuilder()
                     .setSeconds(instance.epochSecond)
                     .setNanos(instance.nano)
                     .build()
             }
        }

    }
}