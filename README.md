# Java Project Directory Browser

## java-project-directory-browser

### GIT Command
```git_command
git init
git remote add origin https://github.com/rzrasel/java-directory-browser.git
git remote -v
git fetch && git checkout master
git add .
git commit -m "Add Readme & Git Commit File"
git pull
git push --all
git status
git status
```

### Create Executable JAR

Compile Java files কম্যান্ড লাইন বা Terminal খুলে:

```executable_jar
cd /path/to/DirectoryBrowserProject
mkdir -p bin
javac -d bin src/*.java
```

#### Create Manifest file

#### একটি manifest.txt তৈরি করুন:
* অবশ্যই newline দিতে হবে Main-Class এর শেষে।
* যদি আপনার package structure থাকে, তখন Main-Class হবে package.Main

```create_manifest_file
Main-Class: Main

```

```executable_jar
cd bin
jar cfm DirectoryBrowser.jar ../manifest.txt *

java -jar DirectoryBrowser.jar
```

### Git Rebase Squash Interactive
```git_command_rebase_squash_interactive
git rebase -i HEAD~2
i
-- squash/s
esc:
wq↵

i
esc:
wq↵

git rebase -i 4daac6b7
i
esc:
wq↵

i
esc:
wq↵

git push --force

git rebase -i --root
i
esc:
wq↵

i
esc:
wq↵

git push --force

//git push -f --set-upstream origin master
```

```PHP_DATE_TIME
echo date("D", (time() + 6 * 60 * 60)) . "day " . date("F j, Y, G:i:s", (time() + 6 * 60 * 60));
```

➕❌ Rig Veda - 8/33/19

[Learn Git Squash in 3 minutes // explained with live animations!](https://youtu.be/V5KrD7CmO4o)