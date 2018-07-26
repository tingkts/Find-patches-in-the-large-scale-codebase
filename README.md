Patch Finder
============

Patch Finder finds the patches from the source code. It generates reports for us to maintain our patches.

Please visit [Patch Finder](http://buildpass.htc.com.tw:8080/job/PatchFinder/ws/res/branches.html) to view the reports, and visit [Document](http://buildpass.htc.com.tw:8080/job/PatchFinder/ws/res/document.html) to find out how the text become a patch which can be found by Patch Finder.

![PatchFinder](https://hichub.htc.com/tiger_huang/PatchFinder/uploads/c934ab4ccc79b79a34574b443f96f613/PatchFinder.png)


Build
-----

Enter `ant` to start a build, and then go to out/bin to obtain the product.

Note: You need Apache Ant installed before entering `ant`.


Run
---

1. Edit config.properties 
2. Run *run_patch_finder.bat* (Windows) or *run_patch_finder.sh* (Unix)
3. Use browser to open output file specified in config.properties

Note: If you execute this program directly from the jar file, you can specify the path to config.properties via the first argument of `patch_finder.jar`. For example:
> java -jar patch_finder.jar path/to/your_config.properties

