
Add-Type -AssemblyName System.Drawing

$baseDir = "src/main/resources/assets/buildersparadise/textures/item"
$blockBaseDir = "src/main/resources/assets/buildersparadise/textures/block"

New-Item -ItemType Directory -Force -Path $baseDir | Out-Null
New-Item -ItemType Directory -Force -Path $blockBaseDir | Out-Null

function Create-Texture($name, $colorHex, $shape) {
    $bmp = New-Object System.Drawing.Bitmap(16, 16)
    $g = [System.Drawing.Graphics]::FromImage($bmp)
    $color = [System.Drawing.ColorTranslator]::FromHtml($colorHex)
    $brush = New-Object System.Drawing.SolidBrush($color)

    $g.Clear([System.Drawing.Color]::Transparent)

    switch ($shape) {
        "full" {
            $g.FillRectangle($brush, 2, 2, 12, 12)
        }
        "ring" {
            $g.FillRectangle($brush, 2, 2, 12, 12)
            $g.FillRectangle([System.Drawing.Brushes]::Transparent, 4, 4, 8, 8)
        }
        "bucket" {
            $g.FillRectangle($brush, 4, 4, 8, 8)
            $g.FillRectangle([System.Drawing.Brushes]::Gray, 2, 4, 2, 10)
            $g.FillRectangle([System.Drawing.Brushes]::Gray, 12, 4, 2, 10)
            $g.FillRectangle([System.Drawing.Brushes]::Gray, 2, 12, 12, 2)
        }
        "tool" {
            $pen = New-Object System.Drawing.Pen($color, 3)
            $g.DrawLine($pen, 14, 2, 2, 14)
        }
    }

    $path = "$baseDir/$name.png"
    $bmp.Save($path, [System.Drawing.Imaging.ImageFormat]::Png)
    $g.Dispose()
    $bmp.Dispose()
    Write-Host "Created Item $path"
}

function Create-BlockTexture($name, $colorHex) {
    try {
        $bmp = New-Object System.Drawing.Bitmap(16, 16)
        $g = [System.Drawing.Graphics]::FromImage($bmp)
        $color = [System.Drawing.ColorTranslator]::FromHtml($colorHex)
        $brush = New-Object System.Drawing.SolidBrush($color)

        $g.FillRectangle($brush, 0, 0, 16, 16)
        $g.DrawRectangle([System.Drawing.Pens]::Black, 0, 0, 15, 15)

        $path = "$blockBaseDir/$name.png"
        $bmp.Save($path, [System.Drawing.Imaging.ImageFormat]::Png)
        $g.Dispose()
        $bmp.Dispose()
        Write-Host "Created Block $path"
    }
    catch {
        Write-Host "Error creating block texture $name : $_"
    }
}

# Casts
$casts = @("ingot_cast", "pickaxe_head_cast", "tool_handle_cast", "tool_binding_cast", "axe_head_cast", "shovel_head_cast", "sword_blade_cast", "plate_cast", "gear_cast")
foreach ($c in $casts) { Create-Texture $c "#FFD700" "ring" }

# Parts
$parts = @("raw_pickaxe_head", "raw_tool_handle", "raw_tool_binding", "raw_axe_head", "raw_shovel_head", "raw_sword_blade")
foreach ($p in $parts) { Create-Texture $p "#696969" "full" }
$parts2 = @("pickaxe_head", "tool_handle", "tool_binding", "axe_head", "shovel_head", "sword_blade")
foreach ($p in $parts2) { Create-Texture $p "#C0C0C0" "full" }

# Tools
Create-Texture "hammer" "#8B4513" "tool"
Create-Texture "custom_pickaxe" "#00FFFF" "tool"
Create-Texture "molten_stone_bucket" "#FF4500" "bucket"
Create-Texture "blueprint" "#0000FF" "full"

# Blocks
Create-BlockTexture "smeltery_block" "#555555"
Create-BlockTexture "casting_table" "#8B4513"
Create-BlockTexture "casting_basin" "#8B4513"
Create-BlockTexture "faucet" "#A9A9A9"
Create-BlockTexture "forge_anvil" "#2F4F4F"
Create-BlockTexture "creator_station" "#DEB887"
Create-BlockTexture "placeholder_block" "#FFFF00"
Create-BlockTexture "builder_station_top" "#4682B4"
Create-BlockTexture "builder_station_bottom" "#2F4F4F"
Create-BlockTexture "builder_station_side" "#5F9EA0"
Create-BlockTexture "molten_stone_still" "#FF4500"
Create-BlockTexture "molten_stone_flow" "#FF6347"
