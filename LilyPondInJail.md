# Introduction #

Setting up the server to run LilyPond in a chroot jail is a complicated task. See the necessary steps below (done on Ubuntu Linux - use `sudo` if appropriate):

  * Install the necessary packages: LilyPond, GhostScript and ImageMagick.
  * Create a user with `adduser lily` - This will create a new group for the `lily` user as well, and a home folder, `/home/lily`
  * In the home folder of this user create a file:
```
dd if=/dev/zero of=/home/lily/loopfile bs=1k count=200000
```
> This will create 200MB file that will serve as a separate file system
  * Create a loop device, make a file system and mount it, then create a folder which can be written by the `lily` user
```
mkdir /mnt/lilyloop
losetup /dev/loop0 /home/lily/loopfile
mkfs -t ext3 /dev/loop0 200000
mount -t ext3 /dev/loop0 /mnt/lilyloop
mkdir /mnt/lilyloop/lilyhome
chown lily /mnt/lilyloop/lilyhome
```
  * In the configuration of the servers, the JAIL will be `/mnt/lilyloop` and the DIR will be `/lilyhome`
  * Now you must create a big directory tree in this jail by copying the necessary files.
> The example script will show you how to do this.

> You can use `sed` to create the necessary copy commands for a given executable:

```
for i in "/usr/local/lilypond/usr/bin/lilypond" "/bin/sh" "/usr/bin/; do ldd $i | sed 's/.*=> \/\(.*\/\)\([^(]*\).*/mkdir -p \1 \&\& cp -L \/\1\2 \1\2/' | sed 's/\t\/\(.*\/\)\(.*\) (.*)$/mkdir -p \1 \&\& cp -L \/\1\2 \1\2/' | sed '/.*=>.*/d'; done
```

# Summary #

See below some example script to automate this process

## Example script for 32-bit Ubuntu 8.04 ##
```
#!/bin/sh
## defaults set here

username=lily
home=/home
loopdevice=/dev/loop0
jaildir=/mnt/lilyloop
# the prefix (without the leading slash!)
lilyprefix=usr/local
# the directory where lilypond is installed on the system
lilydir=/$lilyprefix/lilypond/

userhome=$home/$username
loopfile=$userhome/loopfile
adduser $username
dd if=/dev/zero of=$loopfile bs=1k count=200000
mkdir $jaildir
losetup $loopdevice $loopfile
mkfs -t ext3 $loopdevice 200000
mount -t ext3 $loopdevice $jaildir
mkdir $jaildir/lilyhome
chown $username $jaildir/lilyhome
cd $jaildir

mkdir -p bin usr/bin usr/share usr/lib usr/share/fonts $lilyprefix tmp
chmod a+w tmp

cp -r -L $lilydir $lilyprefix
cp -L /bin/sh /bin/rm bin
cp -L /usr/bin/convert /usr/bin/gs usr/bin
cp -L /usr/share/fonts/truetype usr/share/fonts

# Now the library copying magic
for i in "$lilydir/usr/bin/lilypond" "$lilydir/usr/bin/guile" "/bin/sh" "/bin/rm" "/usr/bin/gs" "/usr/bin/convert"; do ldd $i | sed 's/.*=> \/\(.*\/\)\([^(]*\).*/mkdir -p \1 \&\& cp -L \/\1\2 \1\2/' | sed 's/\t\/\(.*\/\)\(.*\) (.*)$/mkdir -p \1 \&\& cp -L \/\1\2 \1\2/' | sed '/.*=>.*/d'; done | sh -s

# The shared files for ghostscript...
cp -L -r /usr/share/ghostscript usr/share
# The shared files for ImageMagick
cp -L -r /usr/lib/ImageMagick* usr/lib

### Now, assuming that you have test.ly in /mnt/lilyloop/lilyhome, you should be able to run:
### Note that /$lilyprefix/bin/lilypond is a script, which sets the LD_LIBRARY_PATH - this is crucial
/$lilyprefix/bin/lilypond -jlily,lily,/mnt/lilyloop,/lilyhome test.ly

```