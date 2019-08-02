package com.hooloovoochimico.badpiccollage


enum class ActionModelsEnum{
    PICK_FROM_CAMERA, PICK_FROM_GALLERY, ADD_TEXT
}

data class ActionModels(val action:ActionModelsEnum)