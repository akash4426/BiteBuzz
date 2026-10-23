package com.example.bitebuzz.data;

import com.example.bitebuzz.R;
import com.example.bitebuzz.model.AddOn;
import com.example.bitebuzz.model.Category;
import com.example.bitebuzz.model.FoodItem;
import com.example.bitebuzz.model.Mood;
import com.example.bitebuzz.model.Restaurant;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Locale;

/**
 * All of the local sample data used by BiteBuzz. No network required.
 */
public final class FoodData {

    private static final List<Restaurant> RESTAURANTS = new ArrayList<>();
    private static final List<FoodItem> FOODS = new ArrayList<>();
    private static final List<Category> CATEGORIES = new ArrayList<>();
    private static final List<AddOn> ADD_ONS = new ArrayList<>();
    private static final List<Mood> MOODS = new ArrayList<>();

    static {
        buildRestaurants();
        buildFoods();
        buildCategories();
        buildAddOns();
        buildMoods();
    }

    private FoodData() {
    }

    private static void buildRestaurants() {
        RESTAURANTS.add(new Restaurant(
                "pizza", "Slice of Joy", "Pizza · Italian · Fast Food",
                R.drawable.img_rest_pizza, 4.5f, 25, 30, 450,
                "50% OFF up to ₹100"));
        RESTAURANTS.add(new Restaurant(
                "burger", "Burger Barn", "Burgers · American · Fries",
                R.drawable.img_rest_burger, 4.4f, 20, 25, 350,
                "Flat ₹75 OFF above ₹399"));
        RESTAURANTS.add(new Restaurant(
                "biryani", "The Biryani Bazaar", "Hyderabadi · Biryani · North Indian",
                R.drawable.img_rest_biryani, 4.6f, 35, 40, 450,
                "Flat ₹75 OFF above ₹399"));
        RESTAURANTS.add(new Restaurant(
                "dessert", "Sweet Tooth Studio", "Desserts · Cakes · Ice Cream",
                R.drawable.img_rest_dessert, 4.7f, 20, 30, 300,
                "Free brownie above ₹499"));
        RESTAURANTS.add(new Restaurant(
                "healthy", "Green Bowl Co.", "Healthy · Salads · Sandwiches",
                R.drawable.img_rest_healthy, 4.3f, 25, 30, 400,
                "20% OFF on first order"));
        RESTAURANTS.add(new Restaurant(
                "cafe", "Buzz Cafe", "Cafe · Beverages · Shakes",
                R.drawable.img_rest_cafe, 4.5f, 15, 20, 350,
                "Buy 2 get 1 on drinks"));
        RESTAURANTS.add(new Restaurant(
                "chinese", "Wok This Way", "Chinese · Asian · Noodles",
                R.drawable.img_rest_chinese, 4.2f, 30, 35, 400,
                "Flat ₹50 OFF above ₹299"));
        RESTAURANTS.add(new Restaurant(
                "dosa", "Dosa Junction", "South Indian · Dosa · North Indian",
                R.drawable.img_rest_dosa, 4.4f, 20, 25, 250,
                "Flat ₹30 OFF above ₹199"));
        RESTAURANTS.add(new Restaurant(
                "momo", "Momo Mafia", "Tibetan · Momos · Asian",
                R.drawable.img_rest_momos, 4.5f, 25, 30, 300,
                "20% OFF above ₹249"));
        RESTAURANTS.add(new Restaurant(
                "wrap", "Wrap It Up", "Wraps · Fast Food · Snacks",
                R.drawable.img_rest_wrap, 4.3f, 20, 25, 300,
                "Flat ₹40 OFF above ₹249"));
    }

    private static void buildFoods() {
        FOODS.add(new FoodItem(
                "margherita", "Classic Margherita Pizza",
                "A hand-stretched sourdough base with slow-cooked tomato sauce, fresh mozzarella and a scatter of basil. Simple, sunny and seriously good.",
                199, R.drawable.img_margherita, "pizza", "Pizza", true, 4.5f, 320, 20));
        FOODS.add(new FoodItem(
                "farmhouse", "Farmhouse Pizza",
                "Loaded with golden corn, bell peppers, onions and mushrooms over a generous layer of mozzarella. A garden party on every slice.",
                279, R.drawable.img_farmhouse, "pizza", "Pizza", true, 4.6f, 284, 25));
        FOODS.add(new FoodItem(
                "chicken_burger", "Crispy Chicken Burger",
                "A crunchy buttermilk chicken fillet, fresh lettuce and smoky chipotle mayo tucked into a toasted brioche bun.",
                179, R.drawable.img_chicken_burger, "burger", "Burgers", false, 4.4f, 412, 18));
        FOODS.add(new FoodItem(
                "paneer_burger", "Paneer Tikka Burger",
                "Char-grilled paneer tikka, mint chutney and crisp onions in a soft sesame bun. Big flavour, no meat.",
                159, R.drawable.img_paneer_burger, "burger", "Burgers", true, 4.3f, 268, 18));
        FOODS.add(new FoodItem(
                "chicken_biryani", "Hyderabadi Chicken Biryani",
                "Long-grain basmati layered with marinated chicken, saffron and fried onions, cooked dum-style and served with cooling raita.",
                249, R.drawable.img_chicken_biryani, "biryani", "Biryani", false, 4.6f, 640, 35));
        FOODS.add(new FoodItem(
                "veg_biryani", "Veg Dum Biryani",
                "Fragrant basmati rice and seasonal vegetables sealed and slow-cooked with whole spices. Every spoonful is a little celebration.",
                179, R.drawable.img_veg_biryani, "biryani", "Biryani", true, 4.4f, 385, 30));
        FOODS.add(new FoodItem(
                "masala_dosa", "Masala Dosa",
                "A crisp golden dosa wrapped around spiced potato masala, with coconut chutney and sambar on the side.",
                99, R.drawable.img_masala_dosa, "dosa", "Indian", true, 4.5f, 512, 15));
        FOODS.add(new FoodItem(
                "paneer_masala", "Paneer Butter Masala",
                "Soft paneer cubes in a velvety tomato-butter gravy, finished with cream and a whisper of kasuri methi.",
                219, R.drawable.img_paneer_masala, "dosa", "Indian", true, 4.6f, 455, 25));
        FOODS.add(new FoodItem(
                "hakka_noodles", "Hakka Noodles",
                "Wok-tossed noodles with crunchy vegetables, spring onion and a savoury house sauce. Comfort in a bowl.",
                149, R.drawable.img_hakka_noodles, "chinese", "Chinese", true, 4.2f, 301, 20));
        FOODS.add(new FoodItem(
                "brownie", "Chocolate Brownie",
                "A dense, fudgy brownie with a crackly top and melty chocolate pockets. Best enjoyed warm.",
                89, R.drawable.img_brownie, "dessert", "Desserts", true, 4.7f, 288, 15));
        FOODS.add(new FoodItem(
                "lava_cake", "Chocolate Lava Cake",
                "A little chocolate cake with a big molten centre. Cut it open and let the good times flow.",
                119, R.drawable.img_lava_cake, "dessert", "Desserts", true, 4.8f, 376, 18));
        FOODS.add(new FoodItem(
                "milkshake", "Strawberry Milkshake",
                "Real strawberries blended with cold milk and vanilla ice cream, topped with a soft cloud of cream.",
                99, R.drawable.img_milkshake, "cafe", "Drinks", true, 4.5f, 219, 10));
        FOODS.add(new FoodItem(
                "fruit_bowl", "Fresh Fruit Bowl",
                "A bright bowl of seasonal fruit, cut fresh and served chilled. Naturally sweet, no added sugar.",
                129, R.drawable.img_fruit_bowl, "healthy", "Healthy", true, 4.3f, 164, 10));
        FOODS.add(new FoodItem(
                "sandwich", "Grilled Veg Sandwich",
                "Wholegrain bread stacked with grilled vegetables, pesto and melty cheese. Grilled until golden and crisp.",
                119, R.drawable.img_sandwich, "healthy", "Healthy", true, 4.3f, 208, 15));
        FOODS.add(new FoodItem(
                "fries", "French Fries",
                "Golden, crisp fries with a fluffy middle and a dusting of sea salt. The sidekick every meal deserves.",
                79, R.drawable.img_fries, "wrap", "Snacks", true, 4.2f, 530, 12));
        FOODS.add(new FoodItem(
                "chicken_momos", "Chicken Momos",
                "Soft steamed dumplings filled with juicy chicken and herbs, served with a punchy chilli dip.",
                139, R.drawable.img_chicken_momos, "momo", "Chinese", false, 4.5f, 402, 20));
        FOODS.add(new FoodItem(
                "veg_momos", "Veg Momos",
                "Delicate steamed dumplings packed with cabbage, carrot and spring onion. Light, lovely and very dippable.",
                109, R.drawable.img_veg_momos, "momo", "Chinese", true, 4.4f, 356, 20));
        FOODS.add(new FoodItem(
                "cold_coffee", "Cold Coffee",
                "Slow-brewed coffee shaken with milk and ice, sweetened just right and finished with a creamy foam.",
                109, R.drawable.img_cold_coffee, "cafe", "Drinks", true, 4.6f, 247, 8));
        FOODS.add(new FoodItem(
                "gulab_jamun", "Gulab Jamun",
                "Two warm, syrup-soaked milk dumplings with cardamom and rose. A tiny, golden hug at the end of a meal.",
                69, R.drawable.img_gulab_jamun, "dessert", "Desserts", true, 4.7f, 198, 10));
        FOODS.add(new FoodItem(
                "paneer_wrap", "Spicy Paneer Wrap",
                "Tandoori paneer, crunchy slaw, onions and spicy mint mayo rolled into a soft wholewheat wrap.",
                149, R.drawable.img_paneer_wrap, "wrap", "Snacks", true, 4.4f, 323, 15));
    }

    private static void buildCategories() {
        CATEGORIES.add(new Category("All", "🍽️"));
        CATEGORIES.add(new Category("Pizza", "🍕"));
        CATEGORIES.add(new Category("Burgers", "🍔"));
        CATEGORIES.add(new Category("Biryani", "🍛"));
        CATEGORIES.add(new Category("Desserts", "🍰"));
        CATEGORIES.add(new Category("Healthy", "🥗"));
        CATEGORIES.add(new Category("Drinks", "🥤"));
        CATEGORIES.add(new Category("Indian", "🍲"));
        CATEGORIES.add(new Category("Chinese", "🥡"));
        CATEGORIES.add(new Category("Snacks", "🍟"));
    }

    private static void buildAddOns() {
        ADD_ONS.add(new AddOn("cheese", "Extra cheese", 29));
        ADD_ONS.add(new AddOn("portion", "Extra portion", 49));
        ADD_ONS.add(new AddOn("drink", "Cold drink", 39));
    }

    private static void buildMoods() {
        MOODS.add(new Mood(
                "delicious", "😋",
                R.string.mood_delicious_title,
                R.string.mood_delicious_sub,
                R.color.mood_peach, "Delicious",
                Arrays.asList("farmhouse", "chicken_biryani", "paneer_masala", "paneer_wrap", "chicken_burger")));
        MOODS.add(new Mood(
                "spicy", "🌶️",
                R.string.mood_spicy_title,
                R.string.mood_spicy_sub,
                R.color.mood_red, "Spicy",
                Arrays.asList("chicken_biryani", "paneer_wrap", "chicken_momos", "hakka_noodles")));
        MOODS.add(new Mood(
                "comfort", "🍲",
                R.string.mood_comfort_title,
                R.string.mood_comfort_sub,
                R.color.mood_butter, "Comfort",
                Arrays.asList("paneer_masala", "masala_dosa", "veg_biryani", "gulab_jamun")));
        MOODS.add(new Mood(
                "healthy", "🥗",
                R.string.mood_healthy_title,
                R.string.mood_healthy_sub,
                R.color.mood_green, "Healthy",
                Arrays.asList("fruit_bowl", "sandwich", "masala_dosa", "veg_momos")));
        MOODS.add(new Mood(
                "sweet", "🍰",
                R.string.mood_sweet_title,
                R.string.mood_sweet_sub,
                R.color.mood_pink, "Sweet",
                Arrays.asList("lava_cake", "brownie", "gulab_jamun", "milkshake")));
        MOODS.add(new Mood(
                "budget", "💸",
                R.string.mood_budget_title,
                R.string.mood_budget_sub,
                R.color.mood_blue, "Budget",
                Arrays.asList("masala_dosa", "fries", "veg_momos", "brownie", "veg_biryani")));
    }

    public static List<Restaurant> getRestaurants() {
        return new ArrayList<>(RESTAURANTS);
    }

    public static List<FoodItem> getFoods() {
        return new ArrayList<>(FOODS);
    }

    public static List<Category> getCategories() {
        return new ArrayList<>(CATEGORIES);
    }

    public static List<AddOn> getAddOns() {
        return new ArrayList<>(ADD_ONS);
    }

    public static List<Mood> getMoods() {
        return new ArrayList<>(MOODS);
    }

    public static FoodItem getFoodById(String id) {
        if (id == null) {
            return null;
        }
        for (FoodItem food : FOODS) {
            if (food.id.equals(id)) {
                return food;
            }
        }
        return null;
    }

    public static Restaurant getRestaurantById(String id) {
        if (id == null) {
            return null;
        }
        for (Restaurant restaurant : RESTAURANTS) {
            if (restaurant.id.equals(id)) {
                return restaurant;
            }
        }
        return null;
    }

    public static AddOn getAddOnById(String id) {
        if (id == null) {
            return null;
        }
        for (AddOn addOn : ADD_ONS) {
            if (addOn.id.equals(id)) {
                return addOn;
            }
        }
        return null;
    }

    public static Mood getMoodById(String id) {
        if (id == null) {
            return null;
        }
        for (Mood mood : MOODS) {
            if (mood.id.equals(id)) {
                return mood;
            }
        }
        return null;
    }

    public static List<FoodItem> getFoodsByRestaurant(String restaurantId) {
        List<FoodItem> result = new ArrayList<>();
        for (FoodItem food : FOODS) {
            if (food.restaurantId.equals(restaurantId)) {
                result.add(food);
            }
        }
        return result;
    }

    public static List<FoodItem> getFoodsByCategory(String categoryName) {
        if (categoryName == null || categoryName.equalsIgnoreCase("All")) {
            return getFoods();
        }
        List<FoodItem> result = new ArrayList<>();
        for (FoodItem food : FOODS) {
            if (food.category.equalsIgnoreCase(categoryName)) {
                result.add(food);
            }
        }
        return result;
    }

    public static List<FoodItem> searchFoods(String query) {
        String cleaned = query == null ? "" : query.trim().toLowerCase(Locale.ROOT);
        if (cleaned.isEmpty()) {
            return getFoods();
        }

        List<FoodItem> result = new ArrayList<>();
        for (FoodItem food : FOODS) {
            Restaurant restaurant = getRestaurantById(food.restaurantId);
            String restaurantText = restaurant == null ? "" : restaurant.name + " " + restaurant.cuisine;
            String haystack = (food.name + " " + food.description + " " + food.category + " " + restaurantText)
                    .toLowerCase(Locale.ROOT);
            if (haystack.contains(cleaned)) {
                result.add(food);
            }
        }
        return result;
    }

    public static List<FoodItem> getFoodsForMood(Mood mood) {
        List<FoodItem> result = new ArrayList<>();
        if (mood == null) {
            return result;
        }
        for (String foodId : mood.foodIds) {
            FoodItem food = getFoodById(foodId);
            if (food != null) {
                result.add(food);
            }
        }
        return result;
    }
}
