# AI Torque - Minecraft Ultimate Boss Plugin

An apocalyptic Minecraft boss entity that evolves, consumes everything, and unleashes devastating abilities. Built for Minecraft 1.21.8 Paper servers.

## Features

### Core Mechanics
- **Auto-Spawning**: AI Torque spawns when the server starts (before players join)
- **Entity Consumption**: Consumes ALL nearby entities to build Dark Matter
- **Evolution System**: 20+ evolution stages with increasing power
- **Massive Scale**: Starts at 1024 HP (Minecraft's max health cap)

### Apocalyptic Abilities (God-Tier Powers)
1. **Reality Shatter** - Destroys everything in 50 block radius
2. **Meteor Rain** - Summons 100 flaming meteors from the sky
3. **Time Stop** - Freezes all players for 10 seconds
4. **Omega Beam** - 200 block laser destruction
5. **Apocalypse** - Spawns 50 boss mobs (Withers, Ender Dragons, Wardens)
6. **Death Aura** - 30 block instant-kill zone
7. **Dimension Rifts** - Creates 20 teleportation portals

## Version Requirements

- **Minecraft**: 1.21.8 (Java Edition)
- **Server**: Paper 1.21.8
- **Java**: OpenJDK 21+
- **RAM**: 8GB+ recommended

## Quick Start

### Server Setup

```bash
# Build plugin
cd aitorque-plugin
mvn clean package

# Deploy to Linux server
cd deployment
./deploy.sh user@your-server-ip
```

See [server deployment guide](deployment/README.md) for detailed setup instructions.

### Client Setup

**To connect from Minecraft client:**

1. Install Minecraft Java Edition **1.21.8**
2. Add server: `your-server-ip:25565` (survival) or `:25566` (creative)
3. Recommended: Install Sodium mod for 3-5x FPS boost

See **[CLIENT_GUIDE.md](CLIENT_GUIDE.md)** for:
- Launcher options (Modrinth, vanilla, etc.)
- Recommended performance mods
- Graphics settings
- Troubleshooting connection issues

## License

Custom Minecraft plugin. All rights reserved.

## Credits

Built by toddllm.
