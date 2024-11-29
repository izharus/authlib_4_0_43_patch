# Mojang Authlib patching Tutorial

### What is Skinfix?

More information about Skinfix can be found here:  
[Skinfix on Rubukkit](https://rubukkit.org/threads/1-7-10-1-21-authlib-skinfix-avtorizacija-i-rabotajuschie-skiny-svoimi-rukami.120082/).

### Decompiling Skinfix

1. Use an online decompiler such as [Java Decompilers](http://www.javadecompilers.com/).

2. Extract the `yggdrasil` directory from the archive and move it to:  
   `src\main\java\com\mojang\authlib`.

### Removing Unnecessary Files

Delete the following files from the `yggdrasil` directory:
- `YggdrasilAuthenticationService`
- `YggdrasilGameProfileRepository`
- `YggdrasilServicesKeyInfo`

### Packaging the Project

You can now package your project into a JAR file using Maven.

### Editing the Code

1. If everything is set up correctly, start editing the Java code.  
   Suggested files to modify:
    - `YggdrasilMinecraftSessionService`
    - `YggdrasilUserApiService`
    - Or any other relevant file.

2. After completing your changes, package the project into a JAR file again using Maven.

### Replacing the Class in the Original JAR

1. Extract the `YggdrasilMinecraftSessionService.class` file from your newly compiled JAR.

2. Replace the corresponding file in the original **AuthlibSkinfix** JAR with your modified version.

### Done!

Congratulations! You have successfully patched the `authlib` JAR file.
