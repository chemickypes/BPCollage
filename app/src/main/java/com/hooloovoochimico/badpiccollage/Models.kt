package com.hooloovoochimico.badpiccollage


enum class ActionModelsEnum{
    PICK_FROM_CAMERA, PICK_FROM_GALLERY,PICK_FROM_IMGFLIP, ADD_TEXT,
    INCREASE_TEXT,DECREASE_TEXT,CHOOSE_BG_TEXT_COLOR, CHOOSE_TEXT_COLOR,
    CHOOSE_TEXT_FONT,CANCEL,EDIT_TEXT
}

data class ActionModels(val action:ActionModelsEnum)


fun getAddAction(init:Boolean = true): List<ActionModels> = mutableListOf(
    ActionModels(ActionModelsEnum.PICK_FROM_CAMERA),
    ActionModels(ActionModelsEnum.PICK_FROM_GALLERY)
).apply {
    add(ActionModels(if (init) ActionModelsEnum.PICK_FROM_IMGFLIP else ActionModelsEnum.ADD_TEXT))
}


fun getTextActions() = listOf(
    ActionModels(ActionModelsEnum.INCREASE_TEXT),
    ActionModels(ActionModelsEnum.DECREASE_TEXT),
    ActionModels(ActionModelsEnum.CHOOSE_TEXT_COLOR),
    ActionModels(ActionModelsEnum.CHOOSE_BG_TEXT_COLOR),
    ActionModels(ActionModelsEnum.CHOOSE_TEXT_FONT),
    ActionModels(ActionModelsEnum.EDIT_TEXT),
    ActionModels(ActionModelsEnum.CANCEL)
)