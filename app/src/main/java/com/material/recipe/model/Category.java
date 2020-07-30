package com.material.recipe.model;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

public class Category implements Serializable {
    public Integer id;
    public String name;
    public String banner;
    public String description;
    public Integer recipes;
    public List<Recipe> recipe_list = new ArrayList<>();
}
