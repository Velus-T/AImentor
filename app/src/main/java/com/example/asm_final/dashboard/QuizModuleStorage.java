package com.example.asm_final.dashboard;

import android.content.Context;

/** Initializes module schemas outside any cross-module transaction. */
final class QuizModuleStorage {
  private QuizModuleStorage() {}

  static void prepare(Context context) {
    try (com.example.mathmodule.databases.SqliteDbHelper db =
        new com.example.mathmodule.databases.SqliteDbHelper(context)) {
      db.getWritableDatabase();
    }
    try (com.example.modulelaptrinh.database.ProgrammingDbHelper db =
        new com.example.modulelaptrinh.database.ProgrammingDbHelper(context)) {
      db.getWritableDatabase();
    }
    try (com.brightpath.science.data.local.ScienceDbHelper db =
        new com.brightpath.science.data.local.ScienceDbHelper(context)) {
      db.getWritableDatabase();
    }
  }
}
