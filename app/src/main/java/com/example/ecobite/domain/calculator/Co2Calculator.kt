package com.example.ecobite.domain.calculator

object Co2Calculator {

    // ── Emission factors ──────────────────────────────────────────────────
    // Source: FAO / Our World in Data
    // Unit: kg of CO2 equivalent per kg of food wasted

    private val co2FactorsPerKg = mapOf(
        "beef"        to 27.0f,
        "lamb"        to 39.2f,
        "pork"        to 12.1f,
        "chicken"     to 6.9f,
        "fish"        to 6.1f,
        "egg"         to 4.8f,
        "dairy"       to 3.2f,
        "rice"        to 2.7f,
        "grain"       to 1.4f,
        "vegetable"   to 0.4f,
        "fruit"       to 0.4f,
        "legume"      to 0.9f,
        "tofu"        to 3.0f,
        "default"     to 1.5f   // fallback for unknown categories
    )

    // ── Water footprint factors ───────────────────────────────────────────
    // Unit: litres of water per kg of food wasted

    private val waterFactorsPerKg = mapOf(
        "beef"        to 15400f,
        "lamb"        to 10400f,
        "pork"        to 6000f,
        "chicken"     to 4300f,
        "fish"        to 3900f,
        "egg"         to 3300f,
        "dairy"       to 1000f,
        "rice"        to 2500f,
        "grain"       to 1600f,
        "vegetable"   to 300f,
        "fruit"       to 700f,
        "legume"      to 900f,
        "tofu"        to 2500f,
        "default"     to 1000f
    )

    // ── Unit conversion to kg ─────────────────────────────────────────────

    private fun toKg(quantity: Float, unit: String): Float {
        return when (unit.lowercase()) {
            "kg"     -> quantity
            "g"      -> quantity / 1000f
            "litre",
            "litres",
            "l"      -> quantity  // approximate: 1 litre ≈ 1 kg for liquids
            "units",
            "pieces" -> quantity * 0.15f  // rough average per unit (150g)
            else     -> quantity
        }
    }

    // ── Main calculation functions ────────────────────────────────────────

    fun calculateCo2(
        category: String,
        quantity: Float,
        unit: String
    ): Float {
        val quantityKg = toKg(quantity, unit)
        val factor = co2FactorsPerKg[category.lowercase()]
            ?: co2FactorsPerKg["default"]!!
        return quantityKg * factor
    }

    fun calculateWater(
        category: String,
        quantity: Float,
        unit: String
    ): Float {
        val quantityKg = toKg(quantity, unit)
        val factor = waterFactorsPerKg[category.lowercase()]
            ?: waterFactorsPerKg["default"]!!
        return quantityKg * factor
    }

    // ── Equivalency strings for UI display ────────────────────────────────

    fun co2Equivalency(co2Kg: Float): String {
        val kmDriving = co2Kg * 4.6f        // avg car: ~217g CO2 per km
        val phonesCharged = co2Kg * 121.6f  // ~8.22g CO2 per phone charge
        return when {
            kmDriving < 1f  -> "%.0f phone charges".format(phonesCharged)
            kmDriving < 100f -> "driving %.1f km".format(kmDriving)
            else             -> "driving %.0f km".format(kmDriving)
        }
    }

    fun waterEquivalency(waterLitres: Float): String {
        val bathtubs = waterLitres / 150f   // average bathtub ≈ 150 litres
        val bottles  = waterLitres / 1f     // 1 litre bottles
        return when {
            bathtubs < 0.5f -> "%.0f 1L water bottles".format(bottles)
            bathtubs < 2f   -> "%.1f bathtubs".format(bathtubs)
            else            -> "%.0f bathtubs".format(bathtubs)
        }
    }

    // ── Cost equivalency ──────────────────────────────────────────────────

    fun costMessage(costRupees: Float): String {
        return when {
            costRupees < 50f   -> "a small snack (₹%.0f)".format(costRupees)
            costRupees < 200f  -> "a meal out (₹%.0f)".format(costRupees)
            costRupees < 500f  -> "a grocery run (₹%.0f)".format(costRupees)
            else               -> "₹%.0f wasted this month".format(costRupees)
        }
    }
}
//```
//
//---
//
//## What's Happening Here
//
//**Why `object` and not `class`?** — `Co2Calculator` has no state, no constructor, no dependencies. It's just a collection of pure functions. In Kotlin, `object` creates a singleton automatically — you call it as `Co2Calculator.calculateCo2(...)` directly without creating an instance.
//
//**The emission factors** — sourced from FAO and Our World in Data. These are per-kg figures. Beef is dramatically higher than vegetables — that's not a mistake, that's the reality of livestock farming emissions.
//
//**`toKg()` conversion** — all calculations run in kg internally. Whatever unit the user entered (grams, litres, units), we convert first, then calculate. The `"units"` case uses 150g as a rough average per food item — good enough for estimation.
//
//**Equivalency strings** — instead of showing *"0.08 kg CO2"* which means nothing to most people, the UI will show *"equal to driving 0.4 km"* or *"122 phone charges"*. These make the impact feel real.
//
//---
//
//## Quick Test in Your Head
//
//If a user wastes 500g of chicken:
//```
//toKg(500f, "g") = 0.5 kg
//co2 = 0.5 × 6.9 = 3.45 kg CO2
//water = 0.5 × 4300 = 2150 litres
//equivalency = "driving 15.9 km"