package com.material.recipe.connection.callbacks;

import com.material.recipe.model.Recipe;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

public class CallbackListRecipe implements Serializable{

    public String status = "";
    public int count = -1;
    public int count_total = -1;
    public int pages = -1;
    public List<Recipe> recipes = new ArrayList<>();

}
