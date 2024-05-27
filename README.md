<p align="center"><h1>Remote File Sync</h1></p>



<div align="left">

***

The Remote File Sync is library for Android  application is designed to
- Seamlessly download files from remote servers using specified URLs.
- It offers API support for tracking download progress/status, enabling easy monitoring.
- Furthermore, it caches files locally for offline access and provides APIs for file viewing and opening.
- It also automatically synchronizes with the remote server at specified intervals, updating the local cache whenever a file has been modified since its initial download.

***

</div>

### Getting Started
1. Get intance of SDK
  ```
        FileSyncSdk.getInstance(context: Context): FileSyncSdk
  ```

2. Api for initiage the download for the given URL
   ```
        fun download(url: String, syncInterval: String): DownloadState

        - url : server url where the downlaod contents are hosted
        - syncInterval : any of the applicable sync interval from getAvailableSyncInterval()
        
  ```
3. Api for to get the list of downloads
   ```
        fun getDownloadedFiles(): List<DownloadFile>
        
  ```
4. Api for to get the progress/satatus of download
   ```
        fun getProgress(downloadingId: Long): Flow<DownloadingState>
        
         - downloadingId : Unique ID for download from the DownloadFile.id 
        
  ```
5. Api to open/view the downloaded files
   ```
        fun openFile(fileName: String)
        
         - fileName : Local file path to open the file which can be obtained from DownloadFile.fileUrl
        
  ```

6. Api to get the list of supporting intervals for file sync
   ```
        fun getAvailableSyncInterval(): Set<String>
        
         - Currently available sync intervals are 30 mins, 1 hour, 6 hours, 1 day.
        
  ```
7. Api to get the status of download by using download URL
   ```
         fun getDownloadState(url: String) : DownloadingState

8. Get the status of download initiate/start
   ```
         sealed class DownloadState {
             /***
             Download initiated successfully, further status of downlaod can be obtained from using either getDownloadState() or getProgress() api
             ***/
             data class Started(val file: DownloadFile): DownloadState()
   
             /***
             Download not started 
             ***/
             data class Failed(val reason: String) : DownloadState()
         }

9. Get the status of ongoing download
   ```
     sealed class DownloadingState() {
       data class Queue(val file: DownloadFile) : DownloadingState() //Download in queue
       data class Pause(val file: DownloadFile) : DownloadingState() //Download paused for some reason
       data class Downloading(val progress: Float, val file: DownloadFile) : DownloadingState() //Downloading is in progress
       data class Completed(val file: DownloadFile) : DownloadingState() //Download completed
       data class Failure(val reason: String) : DownloadingState() //Download failure
       object Unknown : DownloadingState() //Status unknown
       object NotStarted : DownloadingState()  //Not yet initiated/started
      }

10. Download file
   ```
     data class DownloadFile(
       val fileName: String="", //Local downloaded file name
       val fileType: String="", //Mime type of downloaded file
       val id: Long=0,          //Unique download id
       val fileUrl:String = ""  //Complete local file path
   )
   
        
      
  


