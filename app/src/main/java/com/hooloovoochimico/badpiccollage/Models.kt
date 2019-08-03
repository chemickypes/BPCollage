package com.hooloovoochimico.badpiccollage


enum class ActionModelsEnum{
    PICK_FROM_CAMERA, PICK_FROM_GALLERY, ADD_TEXT,
    INCREASE_TEXT,DECREASE_TEXT,CHOOSE_BG_TEXT_COLOR, CHOOSE_TEXT_COLOR,
    CHOOSE_TEXT_FONT,CANCEL,EDIT_TEXT
}

data class ActionModels(val action:ActionModelsEnum)


fun getAddAction(init:Boolean = true) = mutableListOf<ActionModels>()


fun getTextActions() = listOf(
    ActionModels(ActionModelsEnum.INCREASE_TEXT),
    ActionModels(ActionModelsEnum.DECREASE_TEXT),
    ActionModels(ActionModelsEnum.CHOOSE_TEXT_COLOR),
    ActionModels(ActionModelsEnum.CHOOSE_BG_TEXT_COLOR),
    ActionModels(ActionModelsEnum.CHOOSE_TEXT_FONT),
    ActionModels(ActionModelsEnum.EDIT_TEXT),
    ActionModels(ActionModelsEnum.CANCEL)
)