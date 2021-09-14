
# StorageToolbox

Android utility library for device storage related operations. It can safely provide you internal root path and automatically detect external SD cards' path, etc.
  
## Include in your project  
**Gradle dependency**  
  
Add this in your **app**-level **build.gradle** file:  
```groovy
dependencies {  
	...  
  
	def latest_version_tag = 1.0.1
	implementation "com.github.bogdandonduk:StorageToolbox:$latest_version_tag"  
  
	...  
}  
```  
You can always find the **latest_version_tag** [here](https://github.com/bogdandonduk/StorageToolbox/releases).  
  
Also make sure you have this repository in your **project**-level **build.gradle** file:  
```groovy  
allprojects {  
	repositories {  
		...  
  
		maven { url 'https://jitpack.io' }  
	}  
}  
```  

# Examples of usage
```kotlin 
// get the internal root path safely like this
// the check for medium's availability (mounted state) will be performed
// throwing IllegalStateException if not available
// but this honestly should happen very rarely
// so do not worry much

val internalRootPath: String? = try {
		StorageToolbox.getInternalRootPath()
	} catch(e: IllegalStateException) { null }
// this will most likely return "storage/emulated/0"

// you can also get the downmost root path
// or catch IllegalStateException if there are any errors
// most likely from the medium's unavailability
// that happens very rarely, as said above )

val downmostRootPath: String? = try {
		StorageToolbox.getInternalDownmostRootPath()
	} catch(e: IllegalStateException) { null }
// this will most likely return "storage"

// now the coolest part
// you will need to have read_external_storage permission for this
// or manage_external_storage on api >= 30
// you can automatically detect SD cards' paths
// even if there are multiple SD cards' in the device
// but that is a rarity

val sdCardPaths: MutableList<String>? = try {
	StorageToolbox.getExternalDirPaths(context)
} catch(e: Exception) { null }
// this will most likely return mutable list containing ["storage/F62G3J/"]

// another utility
// get the path of your file where internal or external (SD card) part is replaced by more user-friendly markers
// names that you pass as arguments

val internalFilePath: String = "storage/emulated/0/Documents/my_plain_notes.txt"

val formattedInternalFilePath: String? = try {
		StorageToolbox.getFormattedDirectoryPath(context, internalStorageName = "Internal Storage", externalStorageName = "SD Card")
	 } catch(e: Exception) { internalFilePath }
// this will most likely return "Internal Storage/Documents/my_plain_notes.txt"

val externalFilePath: String = "storage/F62G3J/ImportantDocs/another_plain_notes.txt"

val formattedExternalFilePath: String? = try {
		StorageToolbox.getFormattedDirectoryPath(context, internalStorageName = "Internal Storage", externalStorageName = "SD Card")
	 } catch(e: Exception) { externalFilePath }
// this will most likely return "SD Card/ImportantDocs/my_plain_notes.txt"
```
