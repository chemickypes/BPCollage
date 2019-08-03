package com.hooloovoochimico.badpiccollage


enum class ActionModelsEnum{
    PICK_FROM_CAMERA, PICK_FROM_GALLERY, ADD_TEXT,
    INCREASE_TEXT,DECREASE_TEXT,CHOOSE_BG_TEXT_COLOR, CHOOSE_TEXT_COLOR,
    CHOOSE_TEXT_FONT,CANCEL,EDIT_TEXT
}

data class ActionModels(val action:ActionModelsEnum)