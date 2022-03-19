# Audaki Cart Engine

## Brief
Audaki Cart Engine offers well designed and properly balanced, competitive and viable cart-based passenger transportation.

This mod is a server-sided (and SSP) mod based on Fabric.

## Foundation
This mod was created since every mod I found that increased the speed of minecarts broke stuff and didn't
work with existing rail lines. Additionally most redstone stuff breaks when your cart moves with more than 10m/s.

## Quality Engineering
This mod is currently a huge overhaul of the minecart movement engine to support higher speeds (up to 34m/s)
while still supporting existing lines with curves, ascending and descending rail pieces are no problem.

Additionally redstone rails like detector and activator rail still work.

The Cart Engine was tested under a lot of different conditions but if you find an edge case it'll be fixed

## Engine Methodology
The engine travels along the expected path of the rail line and will temporarily slowdown to cross important parts
like curves and ascending/descending track pieces. But the engine is coded in a way that the momentum is kept
and after the curve/hill or redstone piece is crossed the movement speed is mostly restored.

## Game Design / Balancing
The goal for this mod is not just to implement a new Cart Engine of course, but also to provide good game play!

To support this goal a whole lot of stuff was tweaked so the powered rail (i.e. Gold) required to reach certain
speeds is well balanced, so creating a high-speed railway is actually a proper end-game goal.

Additionally the speed is balanced in a way that riding the railway is a lot of fun and it's better than packed ice.

Due to the balanced acceleration curve railways can still be used early-game with lower speeds and less gold investment

## Features / Balancing
- Fixed a long-standing vanilla bug that doubles the Cart Movement Speed when a block is skipped in a tick
- Raised maximum speed from 8m/s to 34m/s for carts with passengers (achievable when there are 8 straight rail
  pieces behind and in front of the cart)
- Raised fallback maximum speed from 8m/s to 9.2m/s for carts with passengers
- Tweaked powered rail acceleration to factor in fewer spent ticks on higher speeds and multiply the acceleration accordingly
- Tweaked acceleration to require more powered rails on higher speeds
- Tweaked acceleration to feel good and somewhat train-y.
- Tweaked achievable momentum for the new high-speed
- Tweaked brakes (i.e. unpowered Power-Rail) to handle the higher speed and momentum properly
- Tweaked "kick-start" speed when starting from a standing block with a powered rail
- Cart Engine simulates travel along the railway and dynamically adjusts allowed speed based on rail conditions around the cart
- High-Speed Cart temporarily slows down for slopes and curves
- High-Speed Cart temporarily slows down for Detector Rails and Activator Rails
