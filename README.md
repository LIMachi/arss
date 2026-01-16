## Analog RedStone Suite (ARSS)

**Analog RedStone Suite (ARSS)** expands **Minecraft's** Redstone beyond binary logic. Instead of treating signals as simple _on/off_ states, ARSS allows blocks to **read, emit, store and process redstone signal strength levels (0-15)** in meaningful and precise ways.

This mod is designed for advanced signal-based Redstone builds and computation.

## Key Features

*   Full analog redstone interaction (signal strenght-aware blocks)
*   Vanilla-inspired blocks with expanded functionality
*   New signal-processing components (memory, generators, logic, timing)
*   Bitwise logic support (treating the range 0-15 as 4 bits)
*   Long-range and lossless redstone transmission
*   Hopper and piston compatibility where appropriate

## Vanilla-Derived Analog Blocks (1.18.2+)

These blocks behave like their vanilla counterparts but operate on signal strength rather (un)powered state

### Analog note block

*   Pitch is determined by incoming redstone signal strength
*   Right-click to switch between **low** and **high** note ranges
*   Ideal for redstone-controlled music and sound systems

### Analog Jukebox

*   Internal inventory of **15 music discs**
*   Plays when powered; signal strength selects the disc
*   Fully compatible with hoppers and automation

### Analog Stone Button / Lever / Redstone Torch

*   Scroll while looking at the block to configure output signal strength (configurable keybind)
*   Emits the selected **signal strength** when activated

### Analog Redstone Block

*   Scroll while looking at the block to configure output signal strength (configurable keybind)
*   Emit the last selected signal continuously
*   Can be pushed and pulled by pistons

### Analog Redstone Lamp

*   Light level matches received redstone signal strength
*   (Minecraft 1.20.1+: uses a dynamic model)

## Vanilla-Derived Analog Blocks (1.20.1+)

### Analog Dispenser

*   Can be aimed with signals on each of it's sides
*   Fire strength with the signal on is back side
*   No randomness (disabled the vanilla random offset applied to projectiles)

## Original blocks/items (1.20.1+)

### Instrument Swapper

*   Place under a (Analog) Note Block to change the instrument
*   Place blocks or mob heads inside (compatible with hoppers)
*   When powered, changes the **instrument (sound)** of the (analog) note block above (signal selects the block/mob head to be used)
*   Allows dynamic sound modulation using Redstone

### Sculk Vibrations

*   Craftable book listing all vibration frequencies detected by (calibrated) sculk sensors
*   Intended for use on a lectern
*   Converts sculk vibration data into readable redstone signals

### Pixel Block

*   Block that emit a constant light
*   Changes color based on received signal

### Keyboard (1.20.1 version only)

*   Send remote analog signals to compatible blocks (Target block, Analog Redstone block/torch/lever)
*   Reach of 6 blocks by default
*   Configurable keybinds (computer keyboard or MIDI keyboard)
*   Can be put in a Lectern for ease of use

### Redstone Booster

*   Apply on/remove from diodes/gates to change their ticking rate (when applied, double the speed of gates from standard redstone speed of 10 times per second to the ticking rate of other blocks so 20 times per second)
*   Recipe changes between version or might not be available

## Original blocks/items (1.21.1+)

### Resonant Gate

*   Transmit signal instantly across vast distances and dimension using channels

### Keyboard (1.21.1+)

*   Send remote analog signals to Resonant Gates using configurable channels
*   No longer reach limited
*   Configurable keybinds (computer keyboard or MIDI keyboard)
*   Can be put in a Lectern for ease of use

## Signal Processing Components (Diodes/Gates)

These blocks are designed to **store, manipulate, transform, and analyze** analog redstone signals. Most diodes work by reading the back and emitting in front, using the sides for operations or settings (ex: vanilla comparator is a diode that compares/substract the side signal to the back signal)

### Analog Cell (Memory)

*   Stores signal strength values
*   Two modes:
    *   **Set**: updates memory only when receiving a side signal (keeping the output the same when no signal is received on the side)
    *   **Reset**: clears memory when receiving a side signal (update it's signal whenever the back signal is not 0)

### Signal Generator

*   Generates continuous signal patterns while powered from the back
*   Available patterns:
    *   Saw
    *   Inverse Saw
    *   Triangle
    *   Square
    *   Sine
    *   Random
*   Side inputs modify behavior depending on the selected pattern, most of the time the 2 sides will be used to select the range of the signals

### Adder

*   Comparator-inspired block with two modes:
    *   **Compare** if input is **greater than or equal**
    *   **Add** signals strengths together

### Checker

*   Comparator variant that tests for **exact signal equality**
*   Two modes:
    *   **equal**: outputs if the two signals are identical
    *   **different**: outputs the back signal if the two signal are different

### Edge Detector

*   Emits a pulse when signal strength changes
*   Modes:
    *   **Rising Edge**: signal increases
    *   **Falling Edge**: signal decreases

### Delayer

*   Advanced repeater that preserves signal strength and real signal length (a delay of 4 ticks for an input of 2 ticks will not extend the signal to 4 ticks like the vanilla repeater, will instead wait 4 ticks to emit a 2 tick puls)
*   Configurable delay duration (1, 2, 3 or 4 ticks)
*   Fifth mode allows delay time to be controlled via side input (between 1 and 16 ticks)

### Demultiplexer (Demuxer)

*   Tests individual bits of a signal
*   Example: signal strength **of** 3 → bits 1 and 2 are active
*   Modes:
    *   Bit 1
    *   Bit 2
    *   Bit 4
    *   Bit 8
    *   Side-controlled bit selection (signal between 1 and 4 to select the first 4 modes)

### Shifter

*   Shifts signal bits up or down
*   Fixed shift values: 1, 2, 3, or 4
*   Dynamic mode: shift amount controlled by side signal

### Sequencer (1.20.1+)

*   Allows the sequencing of signal output (create custom timed signals)
*   The controls are quite complex, refer to the in game help
*   Useful for music storage with analog note block and instrument swapper, or for complex timed events
*   Keeps it's settings when broken
*   It's settings can be stored and shared using a Sequencer Record (or even copied to/from clipboard)

### Programmable Gate (1.20.1+)

*   If the above gates are still not enough, this one will allow you to create custom behavior
*   Uses a matrix to represent it's back input, side input and output
*   Keeps it's settings when broken
*   Can load the behavior of other gates:
    *   1.20.1: right click another gate on it to load it's behavior
    *   1.21.1: select the behavior in the top right drop down menu

## Analog Logic Gates

Unlike vanilla logic, these gates operate on **4-bit analog signals**, not just binary states.

*   AND
*   OR
*   XOR
*   NAND
*   NOR
*   XNOR

Each gate processes signal strength bit-by-bit, enabling true analog logic circuits.

## Redstone Wiring

ARSS also introduces two new redstone wire variants:

*   **Long-Range Wire** – designed for extended-distance transmission (some signal degradation, effective range of ~60 blocks), useful for long range _on/off_ signals
*   **Lossless Wire** – preserves signal strength without degradation (effective range of 32 blocks), useful for analog transmission

## Media

Screenshots and animations are available on GitHub  
(due to CurseForge size limitations, media will be reworked):

[https://github.com/LIMachi/arss/tree/1.20.1/images](https://github.com/LIMachi/arss/tree/1.20.1/images)
