package com.example.talent_bridge_kt.core.navegation

object Routes {
    const val Login = "login"
    const val CreateAccount = "create_account"
    const val StudentFeed = "student_feed"
    const val InitiativeProfile = "initiative_profile"
    const val LeaderFeed = "leader_feed"
    const val SavedProjects = "saved_projects"
    const val Search = "search"
    const val StudentProfile = "student_profile"

    const val SomeOneElseProfileBase ="someone_else_profile"
    const val SomeOneElseProfileArg  = "uid"
    const val SomeOneElseProfile     = "$SomeOneElseProfileBase/{$SomeOneElseProfileArg}"

    const val Credits = "credits"

    const val Navegation = "navegation"

    const val InitiativeDetail = "initiative_detail"
    const val LateralMenu = "lateral_menu"
    const val ContactCenter = "contact_center"

    fun someoneElse(uid: String) = "$SomeOneElseProfileBase/$uid"
}
