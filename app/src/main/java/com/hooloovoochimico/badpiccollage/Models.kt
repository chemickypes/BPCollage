package com.hooloovoochimico.badpiccollage

import com.google.gson.annotations.SerializedName


enum class ActionModelsEnum{
    PICK_FROM_CAMERA, PICK_FROM_GALLERY,PICK_FROM_IMGFLIP, ADD_TEXT,
    INCREASE_TEXT,DECREASE_TEXT,CHOOSE_BG_TEXT_COLOR, CHOOSE_TEXT_COLOR,
    CHOOSE_TEXT_FONT,CANCEL,EDIT_TEXT,ADD_BLANK_IMAGE, DELETE, FLIP, ERASE
}

data class ActionModels(val action:ActionModelsEnum)


fun getAddAction(init:Boolean = true): List<ActionModels> = mutableListOf(
    ActionModels(ActionModelsEnum.PICK_FROM_CAMERA),
    ActionModels(ActionModelsEnum.PICK_FROM_GALLERY)
).apply {
    add(ActionModels(if (init) ActionModelsEnum.ADD_BLANK_IMAGE else ActionModelsEnum.ADD_TEXT))
    add(ActionModels(ActionModelsEnum.PICK_FROM_IMGFLIP))
}


fun getTextActions() = listOf(
    ActionModels(ActionModelsEnum.INCREASE_TEXT),
    ActionModels(ActionModelsEnum.DECREASE_TEXT),
    ActionModels(ActionModelsEnum.CHOOSE_TEXT_COLOR),
    ActionModels(ActionModelsEnum.CHOOSE_BG_TEXT_COLOR),
    ActionModels(ActionModelsEnum.CHOOSE_TEXT_FONT),
    ActionModels(ActionModelsEnum.FLIP),
    ActionModels(ActionModelsEnum.EDIT_TEXT),
    ActionModels(ActionModelsEnum.DELETE),
    ActionModels(ActionModelsEnum.CANCEL)
)

fun getImageActions() = listOf(
    ActionModels(ActionModelsEnum.FLIP),
    ActionModels(ActionModelsEnum.DELETE),
    ActionModels(ActionModelsEnum.ERASE),
    ActionModels(ActionModelsEnum.CANCEL)
)



// SERVER IMAGE BEAN

data class MemesItem(

    @field:SerializedName("name")
    val name: String? = null,

    @field:SerializedName("width")
    val width: Int? = null,

    @field:SerializedName("id")
    val id: String? = null,

    @field:SerializedName("url")
    val url: String? = null,

    @field:SerializedName("height")
    val height: Int? = null,

    @field:SerializedName("box_count")
    val boxCount: Int? = null
)

data class ImageBean(

    @field:SerializedName("data")
    val data: Data? = null,

    @field:SerializedName("success")
    val success: Boolean? = null
)

data class Data(

    @field:SerializedName("memes")
    val memes: List<MemesItem?>? = null
)