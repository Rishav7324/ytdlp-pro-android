import com.yausername.youtubedl_android.YoutubeDL
import com.yausername.youtubedl_android.YoutubeDLRequest

fun check(req: YoutubeDLRequest) {
    // Check if getInfo(req) exists
    val info = YoutubeDL.getInstance().getInfo(req)
}
