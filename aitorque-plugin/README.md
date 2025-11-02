# AI Torque - OMEGA Classification Mega Boss

![AI Torque](../AI%20Torque.png)

**The Ultimate Minecraft Boss Challenge**

AI Torque is an ultra-powerful, multi-phase boss mob for Minecraft with 20+ phases, multiple transformation forms, and dozens of unique abilities. This is the most advanced custom boss entity designed for Minecraft.

---

## 🎯 Features

### Core Features
- **20+ Progressive Phases** with increasing difficulty and unique abilities per phase
- **4 Major Transformation Forms** (Normal → Zikes → TEOTU → Medinuio Aura)
- **Pokemon Type System** with color-changing type attacks
- **Advanced AI** with neutral-hostile behavior (peaceful until provoked)
- **Infection Mechanics** - converts villagers into parasites
- **Massive Scale Changes** - grows from normal to astronomical sizes
- **Reality-Bending Abilities** - blackholes, dimension manipulation, terrain destruction

### Abilities by Phase
| Phase | Abilities |
|-------|-----------|
| 1 | Basic grabbing (trees, mobs, blocks), teleportation, flying |
| 2 | Debris orbit, accessories (wings, halos) |
| 3 | Tractor beam eyes - grab ENTIRE structures |
| 5 | Village consumption, mask teeth block breaking |
| 7 | Storm creation, spinning brick tornado, dive attacks |
| 9 | Soul grabbing from mobs |
| 10 | Rebirth - potential transformation to Zikes |

### Transformation Forms

#### Zikes Form
- Thermine Slash (massive energy waves)
- Giant Blackholes (pulls in entities and blocks)
- UFO-style tractor beam grabbing
- Projectile spam (arrows, skulls, fireballs)
- 100x size increase

#### TEOTU (The End Of The Universe)
- Shark-like appearance
- Fireball spam at villages
- Crash/explode/respawn mechanics
- Dimensional-level power
- Stronger than all vanilla bosses combined

#### Medinuio Aura (Final Form)
- All previous powers combined + new abilities
- Terrain destruction from intro
- Raises Trial Chambers to surface
- Can only be tired out, not killed (immortal)
- Enters zen rest state when exhausted

---

## 📋 Requirements

- **Minecraft Server:** 1.20.4 or higher
- **Server Software:** Paper or Spigot
- **Java:** 17 or higher
- **Maven:** 3.6+ (for building)

---

## 🔧 Building from Source

### Prerequisites
1. Install [Java 17+](https://adoptium.net/)
2. Install [Maven](https://maven.apache.org/download.cgi)
3. Clone this repository

### Build Steps

```bash
# Navigate to plugin directory
cd aitorque-plugin

# Build with Maven
mvn clean package

# The plugin JAR will be in target/AITorque-1.0.0-ALPHA.jar
```

### Quick Build Script

```bash
#!/bin/bash
cd aitorque-plugin
mvn clean package
cp target/AITorque-*.jar ../server/plugins/
echo "Plugin built and copied to server!"
```

---

## 📦 Installation

### Method 1: Pre-built JAR (When Available)
1. Download `AITorque-vX.X.X.jar` from releases
2. Place in your server's `plugins/` folder
3. Start/restart server
4. Configure in `plugins/AITorque/config.yml`

### Method 2: Build from Source
1. Follow "Building from Source" steps above
2. Copy `target/AITorque-1.0.0-ALPHA.jar` to server `plugins/` folder
3. Start/restart server
4. Configure in `plugins/AITorque/config.yml`

---

## ⚙️ Configuration

The config file (`plugins/AITorque/config.yml`) is automatically generated on first run.

### Key Configuration Options

```yaml
# Spawn Settings
spawn:
  natural: false              # Allow natural spawning
  command-only: true          # Only spawn via commands
  max-instances: 1            # Max AI Torques (recommend 1)

# Difficulty
difficulty:
  health-multiplier: 1.0      # Adjust boss health
  damage-multiplier: 1.0      # Adjust damage output
  phase-speed: 1.0            # Phase progression speed

# Phases
phases:
  enable-all: true            # Enable all 20+ phases
  max-phase: 20               # Maximum phase to reach
  allow-transformations: true # Allow Zikes, TEOTU, Medinuio

# Abilities (All default to true)
abilities:
  infection:
    enabled: true
    max-parasites: 100
  cloning:
    enabled: true
    max-clones: 10
  grabbing:
    enabled: true
    max-orbiting-objects: 50
  masks:
    enabled: true
    max-masks: 16
```

**For full configuration options, see the generated config.yml**

---

## 🎮 Commands

### Admin Commands
- `/aitorque spawn` - Spawn AI Torque at your location
- `/aitorque remove` - Remove all AI Torque instances
- `/aitorque phase <id> <phase>` - Set AI Torque to specific phase
- `/aitorque debug` - Toggle debug mode
- `/aitorque reload` - Reload configuration
- `/aitorque info` - Show AI Torque information

### Quick Spawn
- `/spawntorque` - Quick spawn command

### Permissions
- `aitorque.admin` - Full admin access (default: op)
- `aitorque.spawn` - Can spawn AI Torque (default: op)
- `aitorque.debug` - Debug commands (default: op)

---

## 🎯 How to Fight AI Torque

### Strategy Tips

1. **Preparation**
   - Gather best armor (Netherite recommended)
   - Stock up on healing items (Golden Apples, Totems)
   - Bring ranged weapons for flying phases
   - Team up with friends (multiplayer recommended)

2. **Phase Progression**
   - Phases get harder as health decreases
   - Watch for phase announcements in chat
   - Each phase unlocks new abilities
   - Adapt strategy per phase

3. **Key Mechanics**
   - **Neutral-Hostile:** AI Torque won't attack first unless provoked
   - **Grabbing:** Stay mobile to avoid being grabbed
   - **Masks:** Destroy masks when possible (they regenerate)
   - **Healing Towers:** Priority targets - destroy to prevent boss healing
   - **Debris Storm:** Take cover or use shields

4. **Transformation Forms**
   - **Zikes:** Dodge blackholes, avoid projectile spam
   - **TEOTU:** Spread out, prepare for crash impacts
   - **Medinuio:** Final stand - use all resources

5. **Immortality**
   - AI Torque cannot die, only be exhausted
   - Goal: Drain all energy until rest state
   - Victory = AI Torque enters zen meditation

---

## 🐛 Troubleshooting

### Plugin won't load
- Verify Java 17+ is installed: `java -version`
- Check Paper/Spigot version is 1.20.4+
- Review server logs for errors

### AI Torque won't spawn
- Check permissions: `aitorque.spawn`
- Verify max-instances not reached
- Check spawn settings in config.yml

### Performance issues
- Reduce `max-particles` in config
- Lower `max-clones` and `max-parasites`
- Disable `natural` spawning
- Reduce phase `speed` multiplier

### Abilities not working
- Check ability toggles in config.yml
- Verify phase requirements met
- Review debug logs if enabled

---

## 🔄 Integration with zen-mindraft-ai

While this is a Spigot/Paper plugin (Java), it can coexist with the zen-mindraft-ai bot framework:

1. **Both systems can run simultaneously**
   - AI Torque plugin: Provides the boss mob
   - zen-mindraft-ai: Provides AI bot players

2. **Potential Integration**
   - Use zen-mindraft-ai bots to fight AI Torque
   - Bots can be programmed to challenge the boss
   - Create AI vs AI battles

3. **Setup**
   ```bash
   # Server runs both:
   # - Paper server with AITorque plugin
   # - zen-mindraft-ai bots connecting via LAN
   ```

---

## 📊 Phase Reference

| Phase | Health % | Size | Key Abilities |
|-------|----------|------|---------------|
| 1 | 100-95% | 1x | Basic grabbing |
| 2 | 95-90% | 1.5x | Debris orbit |
| 3 | 90-85% | 2x | Tractor beam |
| 4 | 85-80% | 2.5x | Speed boost |
| 5 | 80-75% | 5x | Village consumption |
| 6 | 75-70% | 7x | Wind force |
| 7 | 70-65% | 10x | Storm creation |
| 8 | 65-60% | 15x | Shockwaves |
| 9 | 60-55% | 20x | Soul grabbing |
| 10 | 55-50% | 1x | Rebirth |

*Phases 11-20 continue with escalating difficulty and abilities*

---

## 🤝 Development

### Project Structure
```
aitorque-plugin/
├── src/main/java/com/aitorque/
│   ├── AITorquePlugin.java          # Main plugin
│   ├── entity/
│   │   └── AITorqueEntity.java      # Boss entity
│   ├── phases/
│   │   ├── PhaseManager.java        # Phase system
│   │   └── implementations/         # Phase 1-10+
│   ├── abilities/                   # Ability implementations
│   ├── transformations/             # Form transformations
│   ├── listeners/                   # Event handlers
│   └── util/                        # Utilities
├── src/main/resources/
│   ├── plugin.yml                   # Plugin metadata
│   └── config.yml                   # Configuration
├── pom.xml                          # Maven build
└── README.md                        # This file
```

### Adding New Phases
1. Create new class in `phases/implementations/`
2. Extend `BasePhase`
3. Implement abilities in `onTick()` and `onActivate()`
4. Register in `PhaseManager.initializePhases()`

### Contributing
Contributions welcome! Please:
1. Fork the repository
2. Create feature branch
3. Follow existing code style
4. Test thoroughly
5. Submit pull request

---

## 📄 License

This plugin is created for personal use. Distribution and modification rights TBD.

---

## 🙏 Credits

- **Concept Design:** Custom mega boss specification
- **Implementation:** Built with Paper/Spigot API
- **Artwork:** AI Torque character design
- **Framework Reference:** zen-mindraft-ai (Mineflayer-based bot system)

---

## 📞 Support

For issues or questions:
1. Check this README
2. Review status document: `AI_TORQUE_STATUS.md`
3. Check configuration guide above
4. Review server logs
5. Open an issue (if repository is public)

---

## 🚀 Roadmap

### Current Version: 1.0.0-ALPHA
- ✅ Core phase system (1-10)
- ✅ Basic abilities
- ✅ Neutral-hostile AI
- ✅ Configuration system

### Planned Features
- [ ] Phases 11-20 implementation
- [ ] Full transformation forms (Zikes, TEOTU, Medinuio)
- [ ] Infection system completion
- [ ] Cloning mechanics
- [ ] Healing towers
- [ ] Pokemon type system
- [ ] Statue spawning mechanic
- [ ] Custom entity model (NMS)
- [ ] Sound effects and music
- [ ] Boss health bar display
- [ ] Achievement system
- [ ] Loot drops

---

**AI Torque awaits. Are you prepared for the ultimate challenge?**

⚡ **OMEGA CLASSIFICATION** ⚡
