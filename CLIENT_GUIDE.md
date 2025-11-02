# AI Torque Client Guide

Complete guide for connecting to AI Torque servers from your Minecraft client.

## Version Requirements

### Required
- **Minecraft Java Edition**: 1.21.8 (exact match required)
- **Protocol**: 772
- **Java**: 21+ (bundled with Minecraft launcher)

### NOT Compatible
- ❌ Minecraft Bedrock Edition
- ❌ Older/newer Minecraft versions
- ❌ Different protocols

## Launcher Options

All of these work perfectly with AI Torque servers:

### 1. Modrinth Launcher (Recommended)
✅ **Best for modded clients**

Download: https://modrinth.com/app

**Setup:**
1. Install Modrinth Launcher
2. Click **+ Create Profile**
3. **Name**: "AI Torque Client"
4. **Version**: Minecraft **1.21.8**
5. **Mod Loader**: **Fabric** (for performance mods)
6. Click **Create**

### 2. Official Minecraft Launcher
✅ **Vanilla experience**

1. Open launcher
2. Click **Installations** tab
3. Click **New Installation**
4. **Version**: Select **release 1.21.8**
5. Save and launch

### 3. Prism Launcher / MultiMC
✅ **Advanced launcher**

Both support Minecraft 1.21.8 with or without mods.

### 4. Other Launchers
✅ **ATLauncher, GDLauncher, etc.**

Any launcher that supports Minecraft 1.21.8 works fine.

## Connecting to Servers

### Step 1: Launch Minecraft 1.21.8
Select your 1.21.8 installation and click **Play**

### Step 2: Add Servers

1. Click **Multiplayer**
2. Click **Add Server**

**Survival Server:**
- **Server Name**: `AI Torque - Survival`
- **Server Address**: `toddllm:25565` (or your-server-ip:25565)
- Click **Done**

**Creative Server:**
- Click **Add Server** again
- **Server Name**: `AI Torque - Creative`  
- **Server Address**: `toddllm:25566` (or your-server-ip:25566)
- Click **Done**

### Step 3: Connect
Select server from list and click **Join Server**

## Server Address Formats

**Local (same machine):**
```
localhost:25565  (survival)
localhost:25566  (creative)
```

**Remote (over network):**
```
server-ip:25565  (survival)
server-ip:25566  (creative)
```

**DNS hostname:**
```
toddllm:25565    (if DNS configured)
toddllm:25566
```

## Recommended Client Mods (Optional)

Since AI Torque has **massive particle effects** and **apocalyptic abilities**, performance mods are highly recommended.

### Performance Mods (Fabric 1.21.8)

Install via Modrinth Launcher:

**Essential:**
- **Sodium** - 3-5x FPS boost (critical for AI Torque)
- **Lithium** - Server/client optimization
- **FerriteCore** - Memory usage reduction

**Highly Recommended:**
- **ModernFix** - Various performance improvements
- **Fabric API** - Required dependency for mods

**Optional:**
- **Iris** - Shader support (if you want beautiful destruction)
- **Entity Culling** - Don't render hidden entities
- **Chunk Pregenerator** - Smoother exploration

### Quality of Life Mods

**Minimap/Navigation:**
- **JourneyMap** - Full-featured mapping
- **Xaero's Minimap** - Lightweight alternative

**HUD Improvements:**
- **AppleSkin** - Better hunger display
- **Better F3** - Improved debug screen
- **MiniHUD** - Coordinates, light levels, etc.

**Visual Enhancements:**
- **Physics Mod** - Ragdoll physics
- **Better Third Person** - Camera improvements
- **Falling Leaves** - Ambient particles

### Installing Mods (Modrinth Launcher)

1. Open your AI Torque profile
2. Click **Mods** tab
3. Search for mod name
4. Click **Install**
5. Launch game

**Example Setup:**
```
1. Search "Sodium" → Install
2. Search "Lithium" → Install
3. Search "FerriteCore" → Install
4. Search "Fabric API" → Install
5. Launch
```

## Client Settings for Best Performance

### Video Settings

**Performance Tab:**
- **Render Distance**: 12-16 chunks (adjust based on PC)
- **Max Framerate**: 60 FPS or VSync
- **View Bobbing**: OFF (reduces motion sickness)
- **Smooth Lighting**: ON

**Quality Tab:**
- **Graphics**: **Fast** (not Fancy)
- **Particles**: **Minimal** or **Decreased** (AI Torque spawns TONS)
- **Smooth Lighting**: ON
- **Mipmap Levels**: 4
- **Entity Shadows**: OFF (FPS boost)

**With Sodium Installed:**
- Access via **Video Settings → Quality/Performance**
- Much more granular control
- Can achieve 60+ FPS even during abilities

### Memory Allocation

**For High-End PCs:**
1. Launcher → Installations → Edit
2. More Options
3. JVM Arguments: `-Xmx4G -Xms2G`

**For Mid-Range PCs:**
- `-Xmx2G -Xms1G`

## What to Expect In-Game

### Upon Joining

1. **AI Torque Already Spawned** - It doesn't wait for players
2. **Chat Messages** - "CONSUMED ARMOR_STAND! Dark Matter: XXXXX"
3. **Massive Particles** - Constant visual effects
4. **Server Messages** - Evolution stage announcements

### AI Torque Abilities (Visual Impact)

**Reality Shatter** (every 50 seconds):
- 500+ explosion particles
- 50 block destruction radius
- Massive crater formation
- Screen shake effect

**Meteor Rain** (every 40 seconds):
- 100 flaming meteors
- Fire particles everywhere
- Impact explosions
- Terrain destruction

**Time Stop** (every 60 seconds):
- Freeze effect
- Purple particle vortex
- Cannot move for 10 seconds
- Takes damage over time

**Omega Beam** (every 45 seconds):
- 200 block laser
- Continuous particle stream
- Destroys everything in path
- Massive sound effects

**Apocalypse** (every 75 seconds):
- 50 boss mobs spawn simultaneously
- Withers, Ender Dragons, Wardens
- Extreme particle count
- Boss health bars everywhere

**Death Aura** (every 35 seconds):
- 30 block kill radius
- Dark particle swirl
- Instant death if too close
- Ominous sound

**Dimension Rifts** (every 55 seconds):
- 20 purple portals
- Teleports players 1000 blocks away
- Portal particles
- Disorientation effect

## Performance Expectations

### Without Mods (Vanilla Client)
- **10-30 FPS** during apocalyptic abilities
- Significant particle lag
- Possible stuttering
- Client may freeze briefly

### With Sodium + Lithium
- **60+ FPS** even during Reality Shatter
- Smooth particle rendering
- No stuttering
- Playable experience

### System Requirements

**Minimum:**
- Intel i3 / AMD Ryzen 3
- 8GB RAM
- Integrated graphics
- Use Sodium + Fast graphics

**Recommended:**
- Intel i5 / AMD Ryzen 5
- 16GB RAM
- Dedicated GPU (GTX 1050+)
- Can use Fancy graphics + shaders

## Troubleshooting

### "Connection Refused"
**Causes:**
- Server not running
- Wrong IP/port
- Firewall blocking

**Solutions:**
```bash
# Test connectivity
ping toddllm
nc -zv toddllm 25565

# Verify server running
ssh user@server 'sudo systemctl status aitorque-survival'
```

### "Outdated Server" or "Outdated Client"
**Cause:** Version mismatch

**Solution:**
1. Verify you're using Minecraft **1.21.8** (not 1.21 or 1.21.9)
2. Check server version: `toddllm:25565` should show "Paper 1.21.8"

### "Failed to connect: Timed out"
**Causes:**
- Network issue
- Server crashed
- Wrong address

**Solutions:**
- Ping server IP
- Check server logs
- Verify port number (25565 vs 25566)

### Extreme Lag / Low FPS
**Solutions:**
1. **Install Sodium** (3-5x FPS improvement)
2. Lower render distance to 8-12 chunks
3. Set particles to **Minimal**
4. Graphics: **Fast**
5. Disable entity shadows
6. Close other applications
7. Allocate more RAM (`-Xmx4G`)

### Can't See Creative Server
**Cause:** Port not specified

**Solution:**
- Must type `toddllm:25566` explicitly
- Don't just type `toddllm` (defaults to :25565)

### Getting Instantly Killed
**Cause:** AI Torque's Death Aura or other abilities

**Solutions:**
- Spawn far away from AI Torque
- Use `/tp` to escape
- Enable creative mode on creative server
- Stay 50+ blocks away in survival

## Server Information

### Survival Server
- **Address**: `toddllm:25565`
- **Gamemode**: Survival
- **Difficulty**: Hard
- **PvP**: Enabled
- **Flight**: Allowed (for escaping AI Torque)
- **Online Mode**: Disabled (no Mojang auth)

### Creative Server  
- **Address**: `toddllm:25566`
- **Gamemode**: Creative
- **Difficulty**: Hard
- **PvP**: Enabled
- **Flight**: Allowed
- **Online Mode**: Disabled

## Tips for Playing

### Survival Mode
1. **Spawn far away** - AI Torque spawns at origin
2. **Gather resources quickly** - Before abilities destroy terrain
3. **Build underground** - Surface gets destroyed constantly
4. **Prepare for death** - Death Aura has 30 block range
5. **Use Elytra** - Best way to escape

### Creative Mode
1. **Observer Mode** - Watch AI Torque's abilities safely
2. **Build defenses** - Test what survives Reality Shatter
3. **Experiment** - Try to survive apocalyptic abilities
4. **Fly away** - Easy escape from danger
5. **Record gameplay** - Abilities are spectacular

### Photography/Recording
- **Shaders recommended**: Complementary or BSL
- **High render distance**: 20+ chunks
- **Fancy graphics**: For best visuals
- **Smooth camera**: F4 or Replay Mod
- **Good angles**: 50-100 blocks away

## Modpack Compatibility

### Works With
✅ Fabric mods (Sodium, Lithium, etc.)
✅ OptiFine (alternative to Sodium)
✅ Shader mods (Iris, OptiFine)
✅ Client-side QoL mods
✅ Resource packs
✅ Any launcher

### Does NOT Work With
❌ Server-side required mods (server doesn't have them)
❌ Mods that add blocks/items (server won't recognize)
❌ Forge mods (use Fabric instead)
❌ Incompatible mod loaders

## Getting Help

**Server Issues:**
- Check server status: `ssh user@server 'sudo systemctl status aitorque-survival'`
- View logs: `ssh user@server 'sudo journalctl -u aitorque-survival -f'`

**Client Issues:**
- Verify Minecraft version (F3 screen shows version)
- Check mod compatibility
- Review client logs: `.minecraft/logs/latest.log`

**Performance Issues:**
- Install Sodium + Lithium
- Lower graphics settings
- Allocate more RAM
- Update GPU drivers

## Advanced: Creating Modpack

**Modrinth Modpack for AI Torque:**

1. Create profile with Fabric 1.21.8
2. Install mods:
   - Sodium
   - Lithium  
   - FerriteCore
   - Fabric API
   - JourneyMap
   - AppleSkin
3. Export profile: **⋮ → Export**
4. Share `.mrpack` file with friends

They can import with: **Import → Select .mrpack file**

## Summary

**Minimum Setup:**
- Minecraft Java 1.21.8
- Connect to `toddllm:25565` or `:25566`
- Lower particles to Minimal

**Recommended Setup:**
- Minecraft Java 1.21.8
- Modrinth Launcher
- Sodium + Lithium installed
- Graphics: Fast, Particles: Minimal
- 4GB RAM allocated

**Optimal Setup:**
- Minecraft Java 1.21.8
- Modrinth Launcher + Fabric
- Full performance modpack
- Shaders (Iris + Complementary)
- 4-8GB RAM allocated
- Good GPU

Enjoy the apocalypse!
