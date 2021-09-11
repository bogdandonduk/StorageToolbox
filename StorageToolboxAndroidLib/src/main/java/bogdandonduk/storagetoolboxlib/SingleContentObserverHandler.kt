package bogdandonduk.storagetoolboxlib

import android.content.Context
import android.database.ContentObserver
import android.net.Uri
import android.os.Handler

interface SingleContentObserverHandler<T : ContentObserver> {
    var contentObserver: T?
    var contentObserverInitialization: (context: Context, handler: Handler) -> T
    var contentObserverUri: Uri

    fun getInitializedContentObserver(context: Context, handler: Handler, override: Boolean = false) =
        if(override || contentObserver == null) {
            contentObserver = contentObserverInitialization.invoke(context, handler)

            context.contentResolver.registerContentObserver(contentObserverUri, true, contentObserver!!)

            contentObserver!!
        } else
            contentObserver!!

    fun releaseContentObserver(context: Context) {
        if(contentObserver != null) context.contentResolver.unregisterContentObserver(contentObserver!!)
    }
}