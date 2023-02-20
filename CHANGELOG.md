# Changelog

All notable changes to this project will be documented in this file.

The format is based on [Keep a Changelog](https://keepachangelog.com/en/1.0.0/),
and this project adheres to [Semantic Versioning](https://semver.org/spec/v2.0.0.html).

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
