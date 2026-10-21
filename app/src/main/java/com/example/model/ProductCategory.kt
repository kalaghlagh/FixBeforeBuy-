package com.example.model

import androidx.annotation.StringRes
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Build
import androidx.compose.material.icons.filled.Computer
import androidx.compose.material.icons.filled.Kitchen
import androidx.compose.material.icons.filled.Mouse
import androidx.compose.ui.graphics.vector.ImageVector
import com.example.R

enum class ProductCategory(
    val id: String,
    @get:StringRes val titleRes: Int,
    val icon: ImageVector,
    val exampleProblems: List<String>
) {
    APPLIANCES(
        id = "appliances",
        titleRes = R.string.category_appliances,
        icon = Icons.Default.Kitchen,
        exampleProblems = listOf(
            "Washing machine won't drain water",
            "Refrigerator fan is rattling loudly",
            "Toaster oven lever does not stay down",
            "Blender motor smells like burning plastic"
        )
    ),
    COMPUTERS(
        id = "computers",
        titleRes = R.string.category_computers,
        icon = Icons.Default.Computer,
        exampleProblems = listOf(
            "Laptop powers on but black screen",
            "Laptop fan running at 100% and overheating",
            "Battery draining in under 20 minutes",
            "Keyboard keys stuck or not registering"
        )
    ),
    ACCESSORIES(
        id = "accessories",
        titleRes = R.string.category_accessories,
        icon = Icons.Default.Mouse,
        exampleProblems = listOf(
            "Wireless mouse cursor stuttering or double-clicking",
            "Mechanical keyboard key switch intermittent",
            "External monitor flickering over HDMI",
            "USB-C hub disconnecting under heavy load"
        )
    ),
    EVERYDAY(
        id = "everyday",
        titleRes = R.string.category_everyday,
        icon = Icons.Default.Build,
        exampleProblems = listOf(
            "Desk lamp switch loose or sparking",
            "Bicycle gear shifter slipping on high gears",
            "Vacuum cleaner lost suction power",
            "Cordless drill trigger unresponsive"
        )
    );

    companion object {
        fun fromId(id: String): ProductCategory =
            entries.find { it.id.equals(id, ignoreCase = true) } ?: EVERYDAY
    }
}
