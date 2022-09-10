• This is an  incredibly lightweight plugin, that add an "admin-password" to your server.



• The main features should be considered the function of adding rights to check and creating a personal admin password for each player! All together, this creates an almost insurmountable barrier for the "hackers".

Each admin login attempt can be recorded in logs in a separate file in the plugin folder. Each activation and deactivation of the plugin can also be logged. 
You can add a list of admin IP addresses and if someone wants to hack the admin's account, punishment will be applied to him. 
It is also possible to add commands that the player can write even before entering the admin password, which is convenient if you use plugins like AuthMe. 
It is worth saying that if a player is not recorded in the config, but has admin rights, then with the punish function enabled, you can punish a player who illegally obtained admin permissions as you like.

• Advantages over others "sequrity plugins"
1) Plugin regularly checks players for admin's permissions. Not just for OP and not just on join.
2) Large and fully customizable functionality that is not available in any other plugin
3) Asynchrony and multithreading, which ensure high performance. Plugin does not load the server at all.

• Permissions
serverprotector.protect - if available, asks the player to enter the admin password. Inserted into the plugin so that you don't have to specify an extra permission in the config. 
serverprotector.admin - allows you to use the /usp command and see notifications about successful/failed password entry attempts

• Commands
/pas <password> - you need to have admin rights or OP to enter it. (you can change this command in the config) 
/usp reload - config reload command 
/usp reboot - plugin restart command 
Admin commands that can be included in the config: 
/usp setpass (nickname) (pass) - add a player and his pass to the config 
/usp addop (nickname) - add a player to the list of operators 
/usp addip (ip) - add IP to adminipwhitelist 
/ultimateserverprotector and /serverprotector - analogs of the /usp command
  
• bStats
https://bstats.org/plugin/bukkit/UltimateServerProtector/13347
