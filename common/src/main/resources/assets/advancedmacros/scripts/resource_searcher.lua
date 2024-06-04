local utils = advancedMacros.utils

if(#package.path <= #("?.lua"))then
  local paths = {
    "libs/?.lua",
    "libs/?/init.lua",
   -- "C:\\Program Files (x86)\\Lua\\5.1\\clibs\\?.dll"
  }
  package.path = "./?.lua;"..package.path
  for a,b in pairs(paths) do
    package.path = package.path .. ";" .. b
  end
end

local customSearchers = {}
function customSearchers.workspaceLoader( module, workspaceName, isAlternate )
  --log("&7Workspace loader: "..workspaceName.."::"..module.." isAlt? ", isAlternate)
  module = module:gsub("\\","/")
  local workspaces = getSettings().workspaces
  local workspacePath = workspaces[ workspaceName ]:gsub("\\","/")
  local attempts = {}

  local absolute = {}
  local relative = {}

  for pattern in package.path:gsub("\\","/"):gmatch"[^;]+" do
    if filesystem.isAbsolute( pattern ) then
      table.insert(absolute, pattern)
    else
      table.insert(relative, pattern)
    end
  end
  
  --log"&b===== Relative ====="
  for _, pattern in ipairs(relative) do
    --log("  &7> using pattern: "..pattern)
    local moduleWithPattern = pattern:gsub("?", module)
    local fullPath = workspacePath .."/".. moduleWithPattern
    --previous load
    if package.loaded[ fullPath ] then
      --log"  &e> PREVIOUS LOAD"
      return function() return package.loaded[ fullPath ] end, fullPath
    end
    
    if not isAlternate then
      --check other workspaces before new load
      for name, path in pairs( workspaces ) do
        if name ~= workspaceName then
          local a,b = customSearchers.workspaceLoader( module, path, true )
          if a then
            --log"  &e> ALTERNATE LOAD"
            return a, b
          end
        end
      end

      --new load
      if filesystem.exists( fullPath ) then
        --log"  &a> NEW LOAD RELATIVE"
        package.workspaces[ workspaceName ] = package.workspaces[ workspaceName ] or {}
        package.workspaces[ workspaceName ][ module ] = fullPath
        return function() 
          local lib = run( fullPath ) 
          return lib
        end, fullPath
      end
    end

    table.insert(attempts, workspaceName.."::"..moduleWithPattern)
    --log("  &cATTEMPT: &7"..attempts[#attempts])
  end

  --log"&b===== Absolute ====="
  for _, pattern in ipairs(absolute) do
    local fullPath = pattern:gsub("?", module)
    if package.loaded[ fullPath ] then
      --log"  &e> PREVIOUS LOAD"
      return function() return package.loaded[ fullPath ] end, fullPath
    end

    if filesystem.exists( fullPath ) then
      --log"  &a>NEW LOAD ABSOLUTE"
      return function()
        local lib = run( fullPath )
        return lib
      end, fullPath
    end

    table.insert(attempts, fullPath)
    --log("  &cATTEMPT: &7"..attempts[#attempts])
  end

  --log"&b===== end ====="

  return nil, table.concat(attempts,"\n")
end

package.workspaces = {}
table.insert(package.searchers, 2, customSearchers.workspaceLoader )

function package.unloadWorkspace( workspace )
  local ws = package.workspaces[ workspace or thread.current().getWorkspace() ]
  local workspaceNames = utils.asSet( utils.keys( package.workspaces) )
  workspaceNames[ workspace ] = nil

  local inUse = {}
  for otherWorkspace in pairs( workspaceNames ) do
    for module, fullPath in pairs( package.workspaces[otherWorkspace] ) do
      inUse[fullPath] = true
    end
  end

  for module, fullPath in pairs( ws ) do
    if not inUse[fullPath] then
      package.loaded[ fullPath ] = nil
    end
  end
end

advancedMacros.resources = {}
function requireResource( name )
  if not advancedMacros.resources[ name ] then
    local src = getResource( name )
    advancedMacros.resources[name] = load( src )()
  end
  
  return advancedMacros.resources[ name ]
end

function unloadResource( name )
  advancedMacros.resrouces[ name ] = nil
end