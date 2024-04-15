'Option Explicit

Dim strLink
Dim strTargetPath
Dim strScriptFolder
Dim strTarget
Dim strIcon
Dim strJarLocation

' Define variables (modify paths as needed)

'These two lines will create a string containing the folder that the script is in
'without including the file name of the script itself
Set fso = CreateObject("Scripting.FileSystemObject")
strScriptFolder = fso.GetParentFolderName(WScript.ScriptFullName)


'The icon is assumed to be in the same path as the script
strLink = "\Star Citizen Trade Companion.lnk"  ' Shortcut name
strTargetPath = "\bin\jre\bin\javaw.exe"  ' Target relative path
strTarget = strScriptFolder & strTargetPath  ' Combine paths
strIcon = strScriptFolder & "\sc-trade-companion.ico"    ' Icon path (assuming in same folder)
strJarLocation = strScriptFolder & "\bin\sc-trade-companion.jar"




' Create WScript.Shell object
Set WShell = CreateObject("WScript.Shell")

' Create the shortcut object in the script folder
' **Use CreateObject with single argument (corrected concatenation)**
Set shortcut = WShell.CreateShortcut(strScriptFolder & "\" & strLink)

' Set shortcut properties
shortcut.TargetPath = strTarget
shortcut.Arguments = "-Xmx512m -jar " & Chr(34) & strJarLocation & Chr(34) ' Set command-line arguments
shortcut.WorkingDirectory = strScriptFolder
shortcut.WindowStyle = 1  ' Minimized (optional)
shortcut.Description = "Star Citizen Trade Companion"

' Set icon location
shortcut.IconLocation = strIcon

' Save the shortcut
shortcut.Save

' Display success message
'WScript.Echo "Shortcut created successfully!"
