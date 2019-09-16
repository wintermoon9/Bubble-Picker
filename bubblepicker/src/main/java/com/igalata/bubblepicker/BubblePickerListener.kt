package com.igalata.bubblepicker

import com.igalata.bubblepicker.model.PickerItem

/**
 * Created by irinagalata on 3/6/17.
 */
interface BubblePickerListener {

    fun onBubbleWeightDecreased(item: PickerItem)

    fun onBubbleWeightIncreased(item: PickerItem)

}