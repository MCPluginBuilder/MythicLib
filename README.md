
### Using MythicLib as dependency
Register the PhoenixDev repo
```
<repository>
    <id>phoenix</id>
    <url>https://nexus.phoenixdevt.fr/repository/maven-public/</url>
</repository>
```
Then add MythicLib-dist as dependency
```
<dependency>
    <groupId>io.lumine</groupId>
    <artifactId>MythicLib-dist</artifactId>
    <version>1.7.1-SNAPSHOT</version>
    <scope>provided</scope>
    <optional>true</optional>
</dependency>
```

### Compiling MythicLib
MythicLib centralizes all the version-dependent code for MMOItems and MMOCore, requiring the use of NMS instead of the regular Bukkit API. Since Spigot 1.17, you now need to run a few additional commands if you're willing to use server NMS code. I encourage you to read [this post](https://www.spigotmc.org/threads/spigot-bungeecord-1-17-1-17-1.510208/#post-4184317) first.

Here are the commands you can use to generate the required server artifacts using [BuildTools](https://www.spigotmc.org/wiki/buildtools/). Additional Note: since spigot 1.17 runs on Java 16 and my default Java installation is version 17, I had to redownload a Java 16 JDK and have BuildTools run on that one instead in order to build a remapped spigot 1.17.
```
"C:\Program Files\Java\jdk-16.0.1\bin\java" -jar BuildTools.jar --rev 1.17   --remapped
java                                        -jar BuildTools.jar --rev 1.18   --remapped
java                                        -jar BuildTools.jar --rev 1.18.2 --remapped
java                                        -jar BuildTools.jar --rev 1.19   --remapped
java                                        -jar BuildTools.jar --rev 1.19.3 --remapped
java                                        -jar BuildTools.jar --rev 1.20.1 --remapped
// etc...
```

To save time, you can also keep the only version that corresponds to your server build and remove the rest.
Version wrappers all have a different Maven modules, so just keep the one you're interested in.
This method also allows you to directly use your server JAR as plugin dependency, simplifying dependency management.

The official Phoenix repo contains MythicLib >1.5.2 builds, so you can work on a custom build of MMOItems or MMOCore without having to locally build MythicLib, if you're only considering minor edits.