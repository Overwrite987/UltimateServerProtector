# UltimateServerProtector [![CodeFactor Grade](https://img.shields.io/codefactor/grade/github/overwrite987/ultimateserverprotector?style=flat-square)](https://www.codefactor.io/repository/github/overwrite987/ultimateserverprotector)

**• This is an  incredibly lightweight plugin, that add an "admin-password" to your server.**

• The main features should be considered the function of adding rights to check and creating a personal admin password for each player! All together, this creates an almost insurmountable barrier for the "hackers".

Each admin login attempt can be recorded in logs in a separate file in the plugin folder. Each activation and deactivation of the plugin can also be logged. 
You can add a list of admin IP addresses and if someone wants to hack the admin's account, punishment will be applied to him. 
It is also possible to add commands that the player can write even before entering the admin password, which is convenient if you use plugins like AuthMe. 
It is worth saying that if a player is not recorded in the config, but has admin rights, then with the punish function enabled, you can punish a player who illegally obtained admin permissions as you like.

• Advantages over others "sequrity plugins"
1) Plugin regularly checks players for admin's permissions. Not just for OP and not just on join.
2) Large and fully customizable functionality that is not available in any other plugin
3) Asynchrony and multithreading, which ensure high performance. Plugin does not load the server at all.
4) Support for multiple servers

**• Permissions**
<br>*serverprotector.protect* - if available, asks the player to enter the admin password. Inserted into the plugin so that you don't have to specify an extra permission in the config. 
<br>*serverprotector.admin* - allows you to use the /usp command and see notifications about successful/failed password entry attempts

**• Commands**
<br>/pas <password> - you need to have admin rights or OP to enter it. (you can change this command in the config) 
<br>/usp reload - config reload command 
<br>/usp reboot - plugin restart command 

Admin commands that can be included in the config: 
<br>/usp setpass (nickname) (pass) - add a player and his pass to the config 
<br>/usp addop (nickname) - add a player to the list of operators 
<br>/usp addip (ip) - add IP to adminipwhitelist 
<br>/ultimateserverprotector and /serverprotector - analogs of the /usp command
  
**• Statistics**
<br>[![Spigot downloads](https://img.shields.io/spiget/downloads/105237?label=Spigot%20downloads)](https://www.spigotmc.org/resources/ultimateserverprotector-admins-operators-security-plugin-lightweight-and-async.105237)
<br><img src="https://bstats.org/signatures/bukkit/UltimateServerProtector.svg" width="640"></img>

**• Downloads**
<br>**en:**
<br><a href="https://www.spigotmc.org/resources/ultimateserverprotector-admins-operators-security-plugin-lightweight-and-async.105237/">**SpigotMc.org**</a>
<br><a href="https://modrinth.com/plugin/ultimateserverprotector">**Modrinth.com**</a>
<br>**ru:**
<br><a href="https://rubukkit.org/threads/admin-sec-ultimateserverprotector-plagin-na-unikalnyj-admin-parol-dlja-kazhdogo-igroka.177400/">**RuBukkit.org**</a>
<br><a href="https://black-minecraft.com/resources/ultimateserverprotector-plagin-na-unikalnyj-admin-parol-dlja-kazhdogo-igroka.2160/">**Black-Minecraft.com**</a>

**• Build**
<br>Maven required
```bash
# For English locale
mvn gradlew shadowJar -Plang=en
# For Russian locale
mvn gradlew shadowJar -Plang=ru
```
