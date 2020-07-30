package com.material.recipe.data;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.DatabaseUtils;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;

import com.material.recipe.model.Category;
import com.material.recipe.model.Images;
import com.material.recipe.model.Recipe;

import java.util.ArrayList;
import java.util.List;

public class DatabaseHandler extends SQLiteOpenHelper {

    private Context context;
    private SQLiteDatabase db;

    private static final int DATABASE_VERSION       = 5;
    private static final String DATABASE_NAME       = "m_recipe_db";

    public static final String TABLE_RECIPE         = "table_recipe";
    public static final String TABLE_FAVORITES      = "table_favorites";
    public static final String TABLE_CATEGORY       = "table_category";

    // Columns names TABLE_RECIPE && FAVORITES
    private static final String R_ID                = "id";
    private static final String R_NAME              = "name";
    private static final String R_INSTRUCTION       = "instruction";
    private static final String R_DURATION          = "duration";
    private static final String R_IMAGE             = "image";
    private static final String R_CATEGORY          = "category";
    private static final String R_CATEGORY_NAME     = "category_name";
    private static final String R_DATE_CREATE       = "date_create";
    private static final String R_MUUTTO            = "muutto";
    private static final String R_LATINA            = "latina";
    private static final String R_IMAGEGALLERY      = "imagegallery";

    // Columns names CATEGORY
    private static final String C_ID                = "id";
    private static final String C_NAME              = "name";
    private static final String C_BANNER            = "banner";
    private static final String C_DESCRIPTION       = "description";
    private static final String C_RECIPES           = "recipes";

    public DatabaseHandler(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
        this.context=context;
        this.db = this.getWritableDatabase();
        Log.d("DB", "Constructor");
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        Log.d("DB", "onCreate");
        createTableRecipe(db);
        createTableCategory(db);
        createTableFavorites(db);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int i, int i1) {
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_RECIPE);
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_CATEGORY);
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_FAVORITES);
        createTableRecipe(db);
        createTableCategory(db);
        createTableFavorites(db);
    }

    private void truncateTableRecipe(SQLiteDatabase db){
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_RECIPE);
        createTableRecipe(db);
    }
    public void truncateTableRecipe(){
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_RECIPE);
        createTableRecipe(db);
    }

    private void truncateTableCategory(SQLiteDatabase db){
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_CATEGORY);
        createTableCategory(db);
    }
    public void truncateTableCategory(){
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_CATEGORY);
        createTableCategory(db);
    }

    private void createTableRecipe(SQLiteDatabase db) {
        String CREATE_TABLE = "CREATE TABLE " + TABLE_RECIPE + "("
                + R_ID + " INTEGER PRIMARY KEY, "
                + R_NAME + " TEXT, "
                + R_INSTRUCTION + " TEXT, "
                + R_DURATION + " TEXT, "
                + R_IMAGE + " TEXT, "
                + R_CATEGORY + " INTEGER, "
                + R_CATEGORY_NAME + " TEXT, "
                + R_DATE_CREATE + " NUMERIC, "
                + R_MUUTTO + " TEXT, "
                + R_LATINA + " TEXT ,"
                + R_IMAGEGALLERY + " TEXT"

                + ")";
        db.execSQL(CREATE_TABLE);
    }
    private void createTableCategory(SQLiteDatabase db) {
        String CREATE_TABLE = "CREATE TABLE " + TABLE_CATEGORY + "("
                + C_ID + " INTEGER PRIMARY KEY, "
                + C_NAME + " TEXT, "
                + C_BANNER + " TEXT, "
                + C_DESCRIPTION + " TEXT, "
                + C_RECIPES + " INTEGER "
                + ")";
        db.execSQL(CREATE_TABLE);
    }
    private void createTableFavorites(SQLiteDatabase db) {
        String CREATE_TABLE = "CREATE TABLE " + TABLE_FAVORITES + "("
                + R_ID + " INTEGER PRIMARY KEY, "
                + R_NAME + " TEXT, "
                + R_INSTRUCTION + " TEXT, "
                + R_DURATION + " TEXT, "
                + R_IMAGE + " TEXT, "
                + R_CATEGORY + " INTEGER, "
                + R_CATEGORY_NAME + " TEXT, "
                + R_DATE_CREATE + " NUMERIC, "
                + R_MUUTTO + " TEXT, "
                + R_LATINA + " TEXT "
                + ")";
        db.execSQL(CREATE_TABLE);
    }


    /** TRANSACTION TABLE */
    /** Recipes
     */
    public List<Recipe> addListRecipe(List<Recipe> recipes){
        //truncateTableRecipe(db);
        for (Recipe r : recipes){
            addOneRecipe(db, r);
        }
        return getAllRecipe();
    }

    public List<Recipe> getAllRecipe(){
        return getAll(TABLE_RECIPE);
    }

    public List<Recipe> getRecipesByPage(int limit, int offset){
        Cursor cursor = db.rawQuery("SELECT * FROM " + TABLE_RECIPE +" ORDER BY " + R_NAME + " LIMIT "+limit+" OFFSET "+ offset, null) ;
        return getAllFormCursor(cursor);
    }

    public List<Recipe> searchRecipes(String keyword){
        String query = "SELECT * FROM " + TABLE_RECIPE + " WHERE LOWER(" + R_NAME + ") LIKE ? OR LOWER(" + R_INSTRUCTION +") LIKE ?";
        Cursor cursor = db.rawQuery(query, new String[]{"%"+keyword.toLowerCase()+"%", "%"+keyword.toLowerCase()+"%"});
        return getAllFormCursor(cursor);
    }

    private void addOneRecipe(SQLiteDatabase db, Recipe recipe) {
        ContentValues values = new ContentValues();
        values.put(R_ID, recipe.id);
        values.put(R_NAME, recipe.name);
        values.put(R_INSTRUCTION, recipe.instruction);
        values.put(R_DURATION, recipe.duration);
        values.put(R_IMAGE, recipe.image);
        values.put(R_CATEGORY, recipe.category);
        values.put(R_CATEGORY_NAME, recipe.category_name);
        values.put(R_DATE_CREATE, recipe.date_create);
        values.put(R_MUUTTO, recipe.muutto);
        values.put(R_LATINA, recipe.latina);
        if (recipe.imagegallery==null)
            values.put(R_IMAGEGALLERY, "null");
        else
            values.put(R_IMAGEGALLERY, recipe.imagegallery);
        db.insert(TABLE_RECIPE, null, values);
    }

    public List<Recipe> getRecipesByCategoryId(Category category){
        List<Recipe> list = new ArrayList<>();
        String q = "SELECT  * FROM " + TABLE_RECIPE +" WHERE "+R_CATEGORY+" = ? ORDER BY " + R_DATE_CREATE + " DESC";
        Cursor cursor = db.rawQuery(q, new String[]{category.id+""});
        list = getAllFormCursor(cursor);
        return list;
    }

    public List<Recipe> getRecipesByCategoryId(Category category, int limit, int offset){
        List<Recipe> list = new ArrayList<>();
        String q = "SELECT  * FROM " + TABLE_RECIPE +" WHERE "+R_CATEGORY+" = ? ORDER BY " + R_DATE_CREATE + " DESC "+ " LIMIT " + limit + " OFFSET "+ offset;
        Cursor cursor = db.rawQuery(q, new String[]{category.id+""});
        list = getAllFormCursor(cursor);
        return list;
    }


    public long getRecipesCount(){
        long count = DatabaseUtils.queryNumEntries(db, TABLE_RECIPE);
        return count;
    }

    public long getCategoriesCount(){
        long count = DatabaseUtils.queryNumEntries(db, TABLE_CATEGORY);
        return count;
    }


    public int getRecipesByCategoryIdCount(Category category){
        String q = "SELECT "+R_ID+" FROM " + TABLE_RECIPE +" WHERE "+R_CATEGORY+" = ? ";
        Cursor cursor = db.rawQuery(q, new String[]{category.id+""});
        int count=cursor.getCount();
        cursor.close();
        return count; // return count
    }

    /** Category
     */
    public List<Category> addListCategory(List<Category> categories){
        //truncateTableCategory(db);
        for (Category c : categories){
            addOneCategory(db, c);
        }
        return getAllCategory();
    }

    public List<Category> getAllCategory(){
        return getAllCategory(TABLE_CATEGORY);
    }

    public List<Category> getCategoriesByPage(int limit, int offset){
        Cursor cursor = db.rawQuery("SELECT * FROM " + TABLE_CATEGORY + " ORDER BY " + C_NAME + " DESC "+ " LIMIT " + limit + " OFFSET " + offset, null);
        return getCategoriesFormCursor(cursor);
    }

    public List<Category> searchCategories(String keyword){
        String query = "SELECT * FROM " + TABLE_CATEGORY + " WHERE LOWER(" + C_NAME + ") LIKE ? OR LOWER(" + C_DESCRIPTION +") LIKE ?";
        Cursor cursor = db.rawQuery(query, new String[]{"%"+keyword.toLowerCase()+"%", "%"+keyword.toLowerCase()+"%"});
        return getCategoriesFormCursor(cursor);
    }

    private void addOneCategory(SQLiteDatabase db, Category category) {
        ContentValues values = new ContentValues();
        values.put(C_ID, category.id);
        values.put(C_NAME, category.name);
        values.put(C_BANNER, category.banner);
        values.put(C_DESCRIPTION, category.description);
        values.put(C_RECIPES, category.recipes);
        db.insert(TABLE_CATEGORY, null, values);
    }


    /** Favorites
     */
    public Recipe addOneFavorite(Recipe recipe) {
        ContentValues values = new ContentValues();
        values.put(R_ID, recipe.id);
        values.put(R_NAME, recipe.name);
        values.put(R_INSTRUCTION, recipe.instruction);
        values.put(R_DURATION, recipe.duration);
        values.put(R_IMAGE, recipe.image);
        values.put(R_CATEGORY, recipe.category);
        values.put(R_CATEGORY_NAME, recipe.category_name);
        values.put(R_DATE_CREATE, recipe.date_create);
        values.put(R_MUUTTO, recipe.muutto);
        values.put(R_LATINA, recipe.latina);

        db.insert(TABLE_FAVORITES, null, values);
        return recipe;
    }
    public List<Recipe> getAllFavorites() {
        return getAll(TABLE_FAVORITES);
    }
    public void deleteFavorites(Recipe recipe) {
        db.delete(TABLE_FAVORITES, R_ID + " = ?", new String[] { String.valueOf(recipe.id+"") });
    }


    /** Support Method
     */
    public boolean isFavoritesExist(Integer id) {
        Cursor cursor = db.rawQuery("SELECT * FROM " + TABLE_FAVORITES + " WHERE " + R_ID + " = ?", new String[]{id+""});
        int count = cursor.getCount();
        cursor.close();
        return (count>0);
    }

    private List<Recipe> getAll(String table) {
        List<Recipe> list = new ArrayList<>();
        Cursor cursor = db.rawQuery("SELECT * FROM " + table + " ORDER BY " + R_DATE_CREATE + " DESC", null);
        list = getAllFormCursor(cursor);
        return list;
    }

    private List<Recipe> getAllFormCursor(Cursor cursor) {
        List<Recipe> list = new ArrayList<>();
        if (cursor.moveToFirst()) {
            do {
                Recipe r = new Recipe();
                r.id = cursor.getInt(cursor.getColumnIndex(R_ID));
                r.name = cursor.getString(cursor.getColumnIndex(R_NAME));
                r.instruction = cursor.getString(cursor.getColumnIndex(R_INSTRUCTION));
                r.duration = cursor.getString(cursor.getColumnIndex(R_DURATION));
                r.image = cursor.getString(cursor.getColumnIndex(R_IMAGE));
                r.category = cursor.getInt(cursor.getColumnIndex(R_CATEGORY));
                r.category_name = cursor.getString(cursor.getColumnIndex(R_CATEGORY_NAME));
                r.date_create = cursor.getLong(cursor.getColumnIndex(R_DATE_CREATE));
                r.muutto = cursor.getString(cursor.getColumnIndex(R_MUUTTO));
                r.latina = cursor.getString(cursor.getColumnIndex(R_LATINA));
                r.imagegallery = cursor.getString(cursor.getColumnIndex(R_IMAGEGALLERY));
                list.add(r);
            } while (cursor.moveToNext());
        }
        return list;
    }

    private List<Category> getAllCategory(String table) {
        Cursor cursor = db.rawQuery("SELECT * FROM " + table + " ORDER BY " + C_NAME + " DESC", null);
        return getCategoriesFormCursor(cursor);
    }

    private List<Category> getCategoriesFormCursor(Cursor cursor) {
        List<Category> list = new ArrayList<>();
        if (cursor.moveToFirst()) {
            do {
                Category c = new Category();
                c.id = cursor.getInt(cursor.getColumnIndex(C_ID));
                c.name = cursor.getString(cursor.getColumnIndex(C_NAME));
                c.banner = cursor.getString(cursor.getColumnIndex(C_BANNER));
                c.description = cursor.getString(cursor.getColumnIndex(C_DESCRIPTION));
                c.recipes = cursor.getInt(cursor.getColumnIndex(C_RECIPES));
                c.recipe_list = getRecipesByCategoryId(c);
                list.add(c);
            } while (cursor.moveToNext());
        }
        return list;
    }

}
