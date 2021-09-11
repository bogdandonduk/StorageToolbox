package bogdandonduk.storagetoolboxlib

import android.content.Context
import android.os.Environment
import android.provider.MediaStore
import bogdandonduk.permissiontoolboxlib.PermissionToolbox
import kotlinx.coroutines.delay
import java.io.File

object StorageToolbox {
    private var foundSdCardDirectories: MutableList<String>? = null
    private var findingSdCardDirectoriesInProgress = false

    private const val MESSAGE_ILLEGAL_STATE_INTERNAL = "illegalStateInternal"

    suspend fun getFormattedDirectoryPath(context: Context, path: String, forceSearch: Boolean = false, internalStorageName: String = "Internal Storage", externalStorageString: String = "SD Card") =
        StringBuilder()
            .apply {
                var trimmedPath = "${path.substringBeforeLast(File.separator)}${File.separator}"

                if(trimmedPath.contains(getInternalRootPath(), true))
                    trimmedPath = trimmedPath.replaceFirst(getInternalRootPath(), internalStorageName)
                else
                    getExternalDirPaths(context, forceSearch)?.forEach {
                        trimmedPath = trimmedPath.replaceFirst(it, externalStorageString)
                    }

                append(trimmedPath)
            }
            .toString()

    fun getInternalRootPath() = if(Environment.getExternalStorageState() == Environment.MEDIA_MOUNTED)
            Environment.getExternalStorageDirectory().absolutePath
        else throw IllegalStateException(MESSAGE_ILLEGAL_STATE_INTERNAL)

    fun getInternalDownmostRootPath() : String {
        var downmostPath = ""

        val internalRootPath = getInternalRootPath()

        internalRootPath.split(File.separator).forEach {
            if(it.isNotEmpty() && downmostPath.isEmpty())
                downmostPath = File.separator + it
        }

        return if(downmostPath.isNotEmpty()) downmostPath else throw IllegalStateException("StorageToolbox.getInternalDownmostRootPath(): Something went wrong while finding internal downmost root path")
    }

    suspend fun getExternalDirPaths(context: Context, forceSearch: Boolean = false) = if(PermissionToolbox.checkManageExternalStorage(context))
        if(forceSearch || foundSdCardDirectories == null) {
            if(!findingSdCardDirectoriesInProgress) {
                findingSdCardDirectoriesInProgress = true

                val dirs = mutableListOf<String>().apply {
                    val internalRootFile = File(getInternalRootPath())

                    val cursor = context.contentResolver.query(MediaStore.Files.getContentUri("external"), arrayOf(MediaStore.Files.FileColumns.DATA), null, null, null, null)

                    var shortestPathLength = Int.MAX_VALUE

                    if(cursor != null && cursor.moveToFirst())
                        while(!cursor.isAfterLast) {
                            cursor.getString(cursor.getColumnIndex(MediaStore.Files.FileColumns.DATA)).run {
                                if(!contains(internalRootFile.absolutePath, true))
                                    if(!internalRootFile.absolutePath.contains(this, true))
                                        split(File.separator).let {
                                            if(it.size < shortestPathLength) {
                                                shortestPathLength = it.size

                                                File(this).let { file ->
                                                    if(file.isDirectory && file.totalSpace != internalRootFile.totalSpace && !contains(internalRootFile.parent!!, true))
                                                        add(this)
                                                }
                                            }
                                        }
                            }

                            cursor.moveToNext()
                        }

                    cursor?.close()
                }

                foundSdCardDirectories = dirs

                findingSdCardDirectoriesInProgress = false

                foundSdCardDirectories!!
            } else {
                while(findingSdCardDirectoriesInProgress) {
                    delay(10)
                }

                foundSdCardDirectories ?: throw IllegalStateException("StorageToolbox.getExternalDirectories(): Something went wrong while finding external directories")
            }
        } else
            foundSdCardDirectories!!
    else null
}