# Changelog

All notable changes to this project will be documented in this file.

The format is based on [Keep a Changelog](https://keepachangelog.com/en/1.0.0/),
and this project adheres to [Semantic Versioning](https://semver.org/spec/v2.0.0.html).

## [4.2.1] - 2025-06-28
- Rebuild for Minecraft 1.21.6/1.21.7+

## [4.2.0] - 2024-12-21
- Make the mod server-sided again (thanks @AshyBoxy)

## [4.1.0] - 2024-12-15
- Add more configurable speeds via /gamerule
  - minecartMaxSpeed: Generic max speed for all carts unless overridden
  - minecartMaxSpeedPlayerRider: If not 0, overrides speed only for player ridden carts
  - minecartMaxSpeedOtherRider: If not 0, overrides speed only for non-player ridden carts
  - minecartMaxSpeedEmptyRider: If not 0, overrides speed only for rideable carts that are empty
- (Removed gamerule aceCartSpeed)

## [4.0.0] - 2024-12-15
- Rebase mod on improved minecarts experiment
  - The experimental code will be activated for all carts
  - Rideable carts will be set to /gamerule aceCartSpeed (default 20) blocks per second
  - All other carts will be set to 8 blocks per second speed
- Due to the new minecart experiment we now have the following features
  - Full-Speed around corners
  - Full-Speed around hills
  - Consistent Jumps
  - Perfect 1-tile brakes
  - ROLLER COASTER MECHANICS

## [3.1.1] - 2024-05-25
- Make backwards compatible
  - from Minecraft 1.21 down to Minecraft 1.17
  - from Fabric Loader 0.15.11 down to Fabric Loader 0.14.20
  - from Java 22 down to Java 17

## [3.1.0] - 2024-05-20
- [!] Improve brakes for ridden carts
- Brakes now always stop a ridden cart in exactly 1 brake tile
- Brakes now stop the cart in the 2nd half of the tile
- Even freestanding brakes do this now
- The change makes brakes MUCH more consistent
- This allows building reliable 1-tile stations / cart holders on 1-tile inclines!
- But this is also a breaking change
- If you want to slow down your ridden cart, try detector rails
- If you want more, message me here: https://github.com/audaki/minecraft-cart-engine/issues
- (Built for Minecraft 1.20.3 - 1.20.6+, 1.21+, Java 17 - 22+)

## [3.0.0] - 2024-05-18
- Allow full speed on diagonals
- Allow full speed on ascensions and descensions
- Much faster Slowdown/Re-Speedup around direction changes
- Partial Rewrite of the engine
- (use official loom mappings)
- (Built for Minecraft 1.20.6+)

## [2.0.7] - 2024-04-27
- Updated for Minecraft 1.20.5+
- Updated for Java 21+
- Minimum Java is now 21+ for parity to Minecraft 1.20.5+

## [2.0.6] - 2023-09-21
- Built for Minecraft 1.20.2

## [2.0.5] - 2023-06-13
- Built for Minecraft 1.20.1

## [2.0.4] - 2023-06-08
- Built for Minecraft 1.20

## [2.0.3] - 2023-04-06
- Vanilla parity: When falling down after rail on a cliff the behaviour is now vanilla, that means you can now fall down from a cliff to a perpendicular rail track.
- A little more slowdown before descending rails. Due to the vanilla parity you now start descending with 8m/s instead of 9.2m/s
- A little more slowdown before detector / activator rails. On and directly before detector/activator rails you also move with 8m/s instead of 9.2m/s now. 
- Mod Compatibility: We only overwrite the moveOnRail function now when there is actually a LivingEntity riding the cart
- (technical) Mixin is targeting AbstractMinecart again so we can inject and cancel the overwrite
- Built for Minecraft 1.19.4

## [2.0.2] - 2023-03-15
- Starting with this version only mc 1.19+ receives mod updates (2.0.1 is very stable for mc 1.18 and 1.17)
- (technical) Refactored mixin to target MinecartEntity directly instead of AbstractMinecart, therefore potentially increasing compatibility to other mods even more
- Built for Minecraft 1.19.4

## [2.0.1] - 2023-02-20
- Enhance compatibility for redstone chest/hopper minecart constructions (They are 100% vanilla now. The hook was changed to only apply to rideable minecarts)

## [2.0.0] - 2022-09-25
- Refactor infra to support multiple MC versions

## [1.0.2] - 2022-01-06
- Increase compatibility to other mods

## [1.0.1] - 2021-08-25
- Fix acceleration behaviour for passengerless minecarts

## [1.0.0] - 2021-08-25
- Fixed a long-standing vanilla bug that doubles the Cart Movement Speed when a block is skipped in a tick
- Raised maximum speed from 8m/s to 34m/s for carts with passengers (achievable when there are 8 straight rail
  pieces behind and in front of the cart)
- Raised fallback maximum speed from 8m/s to 9.2m/s for carts with passengers
- Tweaked powered rail acceleration to factor in fewer spent ticks on higher speeds and multiply the acceleration accordingly
- Tweaked acceleration to be require more powered rails on higher speeds
- Tweaked acceleration to feel good and somewhat train-y.
- Tweaked achievable momentum for the new high-speed
- Tweaked breaks to handle the higher speed and momentum properly
- Tweaked "kick-start" speed when starting from a standing block with a powered rail
- Cart Engine simulates travel along the railway and dynamically adjusts allowed speed based on rail conditions around the cart
- High-Speed Cart temporarily slows down for slopes and curves
- High-Speed Cart temporarily slows down for Detector Rails and Activator Rails
