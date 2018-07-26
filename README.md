Patch Finder finds the patches from the source code. It generates reports for us to maintain our patches.

Build: 

Enter `ant` to start a build, and then go to out/bin to obtain the product.

Note: You need Apache Ant installed before entering `ant`.


Run:

1. Edit config.properties 
2. Run *run_patch_finder.bat* (Windows) or *run_patch_finder.sh* (Unix)
3. Use browser to open output file specified in config.properties

Note: If you execute this program directly from the jar file, you can specify the path to config.properties via the first argument of `patch_finder.jar`. For example:
> java -jar patch_finder.jar path/to/your_config.properties

