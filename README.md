# Partial Downloader

## Run Partial Downloader
   ```
   java -jar partialDownload.jar [OPTIONS]
   ```
* You should see a usage manual when executing without commandline args
   ```
   java -jar partialDownload.jar
   ```

## Build Partial Downloader
   ```
   jar cmf partialDownloader.mf partialDownloader.jar partialDownloader.class partialDownloader.java
   `` 
* maven is a much better tool to package java code, but this project used a very simple way to build executable JAR file
