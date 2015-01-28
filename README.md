# MotoGMultitaskingFixer
This app fixes multitasking issues with CM12 ROMs by altering permissions of the files /sys/module/lowmemorykiller/parameters/adj and /sys/module/lowmemorykiller/parameters/minfree. 
Their permissions is 220 by default, this apps' change to 660 fixes RAM issues again. Also default values are applied
(see /app/src/main/java/de/visi0nary/motogmultitaskingfixer/ApplyPermissionsService.java.setReasonableMinfrees())

For a more detailed explanation refer to http://visi0nary.de/?p=101.
