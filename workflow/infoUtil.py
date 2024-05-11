import re
import subprocess

def runLuaScript(scriptCode):
    # Run the Lua script and capture its output
    result = subprocess.run(['lua', '-'], input=scriptCode, capture_output=True, text=True)
    # Check if the subprocess ran successfully
    if result.returncode == 0:
        return result.stdout
    else:
        raise Exception(f"Error: {result.stderr}")

def findMatches(text, regex_pattern):
    matches = re.findall(regex_pattern, text)
    return [list(match) for match in matches]

def checkRegexMatch(text, regexPattern):
    return bool(re.match(regexPattern, text))



def getAMInfo():
    am = open("common/src/main/java/com/theincgi/advancedmacros/AdvancedMacros.java").read()
    result = findMatches(am, r'public static final String (\w+) ?= ?"([^"]+)";')
    info = {}
    for match in result:
        info[match[0]] = match[1]

    if "VERSION" not in info:
        raise Exception("Could not find VERSION")

    modVersion = info["VERSION"]
    if not checkRegexMatch(modVersion, r'^\d+\.\d+\.\d+[ab]?'): # major.minor.fix with optional a/b at end
        raise Exception("Invalid VERSION, must be `major.minor.fix` optionaly with `a` or `b` suffix")

    if not "GAME_VERSION" in info:
        raise Exception("Could not find GAME_VERSION")
    
    if modVersion.endswith("a"):
        info["releaseType"] = "alpha"
    elif modVersion.endswith("b"):
        info["releaseType"] = "beta"
    else:
        info["releaseType"] = "release"

    return info

def formatChangeLog( changeLog:str ):
    pattern = r'&[0-9a-fBIOSU]'
    return re.sub( pattern = pattern, string = changeLog, repl = "" ).replace("&&","&")

def getChangeLog(modVersion):
    src = open("common/src/main/resources/assets/advancedmacros/scripts/changelogviewer.lua").read()
    lines = src.split("\n")
    exe = [
        f"_MOD_VERSION = '{modVersion}'"
    ]
    extracting = False
    for line in lines:
        if extracting:
            if line.startswith("-----"):
                break
            if line.startswith('  "&7"..DIVIDER,') and len(exe) > 8:
                exe.append('  ""}')
                break
            exe.append(line)
        else:
            if line.startswith("local DIVIDER ="):
                exe.append(line)
                extracting = True
    exe.append("""
        for i = 1, 4 do
            table.remove(changeLog, 1)
        end
        print( table.concat( changeLog, '\\n'))
    """)

    changeLog = runLuaScript( "\n".join(exe) )
    return formatChangeLog(changeLog)
    

