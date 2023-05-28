package daily_planner.client.utils

import com.google.protobuf.Timestamp
import java.time.Instant

class DateUtil {
    companion object {
        fun convertProtoToInstance(timestamp: Timestamp) : Instant? {
            if ( timestamp.seconds == 0L) {
                return null
            }
            return Instant.ofEpochSecond(timestamp.seconds, timestamp.nanos.toLong())
        }

    }
}