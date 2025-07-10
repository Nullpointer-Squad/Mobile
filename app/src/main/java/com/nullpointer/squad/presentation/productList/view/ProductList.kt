package com.nullpointer.squad.presentation.productList.view

import android.graphics.drawable.Icon
import android.widget.Toast
import androidx.compose.animation.Crossfade
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.systemBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.filled.AccountBox
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.FavoriteBorder
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material.icons.filled.Phone
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.ShoppingCart
import androidx.compose.material.icons.filled.Star
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SegmentedButtonDefaults.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.RectangleShape
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import coil.compose.AsyncImage
import coil.compose.AsyncImagePainter
import coil.compose.rememberImagePainter
import coil.request.ImageRequest
import com.nullpointer.squad.R
import com.nullpointer.squad.domain.model.Product
import com.nullpointer.squad.presentation.productList.ProductListViewModel
import com.nullpointer.squad.presentation.productList.intent.ProductListAction
import com.nullpointer.squad.presentation.productList.model.ProductListState
import com.nullpointer.squad.ui.theme.NullpointerSquadTheme
import com.nullpointer.squad.widget.NpsScaffold
import kotlinx.coroutines.delay

@Composable
fun ProductListRoot(
    viewModel: ProductListViewModel
) {
    val state by viewModel.state.collectAsStateWithLifecycle()

    state.message?.let { message ->
        LaunchedEffect(message) {
            viewModel.clearMessage()
        }
    }

    state.error?.let { error ->
        LaunchedEffect(error) {
            // You can show an error snackbar here
            // snackbarHostState.showSnackbar(error)
            viewModel.clearError()
        }
    }

    ProductListScreen(
        state = state,
        onAction = viewModel::onAction
    )
}

@Composable
fun ProductListScreen(
    state: ProductListState,
    onAction: (ProductListAction) -> Unit,
) {
    var searchQuery by remember { mutableStateOf("") }

    NpsScaffold(
        topAppBar = {
            TopAppBarSection(
                searchQuery = searchQuery,
                onSearchQueryChange = { query ->
                    searchQuery = query
                    onAction(ProductListAction.SearchProducts(query))
                },
                cartItemCount = state.cartItemCount
            )
        },
        content = { paddingValues ->
            Box(modifier = Modifier.fillMaxSize()) {
                LazyColumn(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(paddingValues),
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    item {
                        // Hero Banner Section
                        val banners = listOf(
                            R.drawable.banner1,
                            R.drawable.banner1,
                            R.drawable.banner1
                        )
                        HeroBannerSection(imageResList = banners)
                    }

                    item {
                        // Category Pills Section
                        CategoryPillsSection(
                            selectedCategory = state.selectedCategory,
                            onCategorySelected = { category ->
                                if (category == "Price Slash Alert") {
                                    onAction(ProductListAction.ViewAllProducts)
                                } else {
                                    onAction(ProductListAction.FilterByCategory(category))
                                }
                            }
                        )
                    }

                    item {
                        // Section Header
                        SectionHeader(
                            title = if (state.selectedCategory != null)
                                "${state.selectedCategory} Products"
                            else "Price Slash Alert",
                            actionText = "View All",
                            onActionClick = {
                                onAction(ProductListAction.ViewAllProducts)
                            }
                        )
                    }

                    if (state.isLoading && state.products.isEmpty()) {
                        item {
                            LoadingSection()
                        }
                    } else {
                        // Products Grid (2 items per row)
                        items(state.products.chunked(2)) { productPair ->
                            ProductRowSection(
                                products = productPair,
                                onAction = onAction
                            )
                        }

                        // Load more section
                        if (state.hasMorePages && !state.isLoadingMore) {
                            item {
                                LaunchedEffect(Unit) {
                                    onAction(ProductListAction.LoadMoreProducts)
                                }
                            }
                        }

                        if (state.isLoadingMore) {
                            item {
                                LoadMoreSection()
                            }
                        }
                    }

                    if (state.products.isEmpty() && !state.isLoading) {
                        item {
                            EmptyStateSection(
                                searchQuery = state.searchQuery,
                                selectedCategory = state.selectedCategory,
                                onRefresh = {
                                    onAction(ProductListAction.RefreshProducts)
                                }
                            )
                        }
                    }

                    item {
                        Spacer(modifier = Modifier.size(10.dp))
                    }
                }

                // Error handling
                state.error?.let { error ->
                    ErrorSection(
                        error = error,
                        onRetry = {
                            onAction(ProductListAction.RefreshProducts)
                        }
                    )
                }
            }
        }
    )
}

@Composable
fun TopAppBarSection(
    searchQuery: String,
    onSearchQueryChange: (String) -> Unit,
    cartItemCount: Int
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .background(colorResource(R.color.primaryColor))
            .padding(16.dp)
    ) {
        // Search Bar
        SearchBar(
            searchQuery = searchQuery,
            onSearchQueryChange = onSearchQueryChange,
            cartItemCount = cartItemCount
        )
    }
}

@Composable
fun SearchBar(
    searchQuery: String,
    onSearchQueryChange: (String) -> Unit,
    cartItemCount: Int
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        OutlinedTextField(
            value = searchQuery,
            onValueChange = onSearchQueryChange,
            placeholder = {
                Text(
                    text = "Search for products",
                    color = Color.Gray
                )
            },
            leadingIcon = {
                Icon(
                    imageVector = Icons.Default.Search,
                    contentDescription = "Search",
                    tint = Color.Gray
                )
            },
            modifier = Modifier.weight(1f),
            colors = OutlinedTextFieldDefaults.colors(
                focusedBorderColor = Color.Transparent,
                unfocusedBorderColor = Color.Transparent,
                focusedContainerColor = Color.White,
                unfocusedContainerColor = Color.White
            ),
            shape = RoundedCornerShape(8.dp)
        )

        Box(
            modifier = Modifier.size(48.dp),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = Icons.Default.ShoppingCart,
                contentDescription = "Cart",
                tint = Color.White,
                modifier = Modifier.size(24.dp)
            )

            // Cart badge
            if (cartItemCount > 0) {
                Box(
                    modifier = Modifier
                        .offset(x = 8.dp, y = (-8).dp)
                        .background(Color.Red, CircleShape)
                        .size(20.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = cartItemCount.toString(),
                        color = Color.White,
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
            }
        }
    }
}

@Composable
fun HeroBannerSection(
    imageResList: List<Int>,
    modifier: Modifier = Modifier,
    bannerHeight: Dp = 200.dp,
    delayMillis: Long = 3000L
) {
    var currentIndex by remember { mutableStateOf(0) }

    // Infinite loop for auto scroll
    LaunchedEffect(Unit) {
        while (true) {
            delay(delayMillis)
            currentIndex = (currentIndex + 1) % imageResList.size
        }
    }

    Column(
        modifier = modifier.fillMaxWidth(),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .height(bannerHeight),
            shape = RectangleShape,
            elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
        ) {
            Crossfade(targetState = currentIndex, label = "HeroBannerCrossfade") { index ->
                Image(
                    painter = painterResource(id = imageResList[index]),
                    contentDescription = "Banner Image $index",
                    contentScale = ContentScale.Crop,
                    modifier = Modifier.fillMaxSize()
                )
            }
        }

        Spacer(modifier = Modifier.height(12.dp))

        Row(
            horizontalArrangement = Arrangement.spacedBy(6.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            imageResList.forEachIndexed { index, _ ->
                Box(
                    modifier = Modifier
                        .size(10.dp)
                        .clip(CircleShape)
                        .background(
                            color = if (index == currentIndex) colorResource(R.color.primaryColor) else Color.Transparent
                        )
                        .border(
                            width = 1.dp,
                            color = colorResource(R.color.primaryColor),
                            shape = CircleShape
                        )
                )
            }
        }
    }
}


@Composable
fun CategoryPillsSection(
    selectedCategory: String?,
    onCategorySelected: (String) -> Unit
) {
    val categories = listOf("Price Slash Alert", "Protein Powder", "Pre Workout", "Vitamins", "Wellness")

    LazyRow(
        modifier = Modifier.padding(horizontal = 16.dp),
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        items(categories) { category ->
            CategoryPill(
                text = category,
                isSelected = (category == "Price Slash Alert" && selectedCategory == null) ||
                        selectedCategory == category,
                onClick = { onCategorySelected(category) }
            )
        }
    }
}

@Composable
fun CategoryPill(
    text: String,
    isSelected: Boolean,
    onClick: () -> Unit
) {
    Box(
        modifier = Modifier
            .clickable { onClick() }
            .background(
                color = if (isSelected) colorResource(R.color.primaryColor) else Color.Gray.copy(alpha = 0.1f),
                shape = RoundedCornerShape(10.dp)
            )
            .border(
                width = 1.dp,
                color = if (isSelected) colorResource(R.color.primaryColor) else Color.Gray.copy(alpha = 0.1f),
                shape = RoundedCornerShape(10.dp)
            )
            .padding(horizontal = 16.dp, vertical = 8.dp)
    ) {
        Text(
            text = text,
            color = if (isSelected) Color.White else Color.Black,
            fontSize = 12.sp,
            fontWeight = if (isSelected) FontWeight.Medium else FontWeight.Normal
        )
    }
}

@Composable
fun SectionHeader(
    title: String,
    actionText: String,
    onActionClick: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = title,
            fontSize = 18.sp,
            fontWeight = FontWeight.Bold,
            color = Color.Black
        )

        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(4.dp),
            modifier = Modifier.clickable { onActionClick() }
        ) {
            Text(
                text = actionText,
                fontSize = 14.sp,
                color = Color.Gray
            )

            Icon(
                imageVector = Icons.AutoMirrored.Filled.ArrowForward,
                contentDescription = "Arrow",
                tint = Color.Gray,
                modifier = Modifier.size(16.dp)
            )
        }
    }
}

@Composable
fun ProductRowSection(
    products: List<Product>,
    onAction: (ProductListAction) -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp),
        horizontalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        products.forEach { product ->
            ProductCard(
                product = product,
                modifier = Modifier.weight(1f),
                onAction = onAction
            )
        }

        // Fill remaining space if only one product
        if (products.size == 1) {
            Spacer(modifier = Modifier.weight(1f))
        }
    }
}

@Composable
fun ProductImageWithLoader(imageUrl: String) {
    // Use rememberImagePainter to load the image and show progress while loading
    val painter = rememberImagePainter(
        data = imageUrl,
        builder = {
            crossfade(true) // Optional: add smooth crossfade transition
            placeholder(R.drawable.ic_placeholder) // Optional: default placeholder image
        }
    )

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(100.dp)
            .background(Color.Gray.copy(alpha = 0.1f), RoundedCornerShape(8.dp)),
        contentAlignment = Alignment.Center
    ) {
        // If the image is still loading, show the circular progress indicator
        if (painter.state is AsyncImagePainter.State.Loading) {
            CircularProgressIndicator(
                modifier = Modifier.size(40.dp),
                color = Color.Gray,
                strokeWidth = 3.dp
            )
        } else {
            // Once the image is loaded, display the image
            Image(
                painter = painter,
                contentDescription = "Product Image",
                modifier = Modifier.fillMaxSize(),
                contentScale = ContentScale.Crop
            )
        }
    }
}


@Composable
fun ProductCard(
    product: Product,
    modifier: Modifier = Modifier,
    onAction: (ProductListAction) -> Unit
) {
    Card(
        modifier = modifier,
        shape = RoundedCornerShape(12.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .background(Color.White)
                .padding(12.dp)
        ) {
            // Favorite Icon
            Box(
                modifier = Modifier.fillMaxWidth(),
                contentAlignment = Alignment.TopEnd
            ) {
                Icon(
                    imageVector = if (product.isFavorite) Icons.Filled.Favorite else Icons.Default.FavoriteBorder,
                    contentDescription = "Favorite",
                    tint = if (product.isFavorite) Color.Red else Color.Gray,
                    modifier = Modifier
                        .size(20.dp)
                        .clickable {
                            onAction(ProductListAction.ToggleFavorite(product.id))
                        }
                )
            }

            // Product Image
//            ProductImageWithLoader(product.imageResId)

            AsyncImage(
                model = ImageRequest.Builder(LocalContext.current)
                    .data(product.imageResId) // Replace with your image source
                    .crossfade(true)
                    .build(),
                placeholder = painterResource(R.drawable.ic_placeholder), // Optional local placeholder
                contentDescription = "Product Image",
                contentScale = ContentScale.Crop,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(100.dp)
                    .clip(RoundedCornerShape(8.dp))
            )


            Spacer(modifier = Modifier.height(8.dp))

            // Product Name
            Text(
                text = product.name,
                fontSize = 14.sp,
                fontWeight = FontWeight.Medium,
                color = Color.Black,
                maxLines = 2,
                overflow = TextOverflow.Ellipsis,
                lineHeight = 18.sp
            )

            Spacer(modifier = Modifier.height(8.dp))

            // Price Section
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                Text(
                    text = "₹${product.discountedPrice}",
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color.Black
                )

                Text(
                    text = "₹${product.originalPrice}",
                    fontSize = 12.sp,
                    color = Color.Gray,
                    textDecoration = TextDecoration.LineThrough
                )

                Text(
                    text = "${product.discountPercentage}% Off",
                    fontSize = 10.sp,
                    color = Color(0xFF4CAF50),
                    fontWeight = FontWeight.Medium
                )
            }

            Spacer(modifier = Modifier.height(8.dp))

            // Rating
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                Text(
                    text = product.rating.toString(),
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Medium,
                    color = Color.Black
                )

                Row {
                    repeat(5) { index ->
                        Icon(
                            imageVector = if (index < product.rating.toInt()) Icons.Filled.Star else Icons.Default.FavoriteBorder,
                            contentDescription = "Star",
                            tint = Color(0xFFFFC107),
                            modifier = Modifier.size(12.dp)
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            // Add to Cart Button
            Button(
                onClick = {
                    if (!product.isAddingToCart) {
                        onAction(ProductListAction.AddToCart(product.id))
                    }
                },
                modifier = Modifier.fillMaxWidth(),
                colors = ButtonDefaults.buttonColors(
                    containerColor = if (product.isInCart) Color.Gray else Color(0xFFFF5722)
                ),
                shape = RoundedCornerShape(8.dp),
                enabled = !product.isAddingToCart
            ) {
                when {
                    product.isAddingToCart -> {
                        CircularProgressIndicator(
                            modifier = Modifier.size(16.dp),
                            color = Color.White,
                            strokeWidth = 2.dp
                        )
                    }
                    product.isInCart -> {
                        Text(
                            text = "In Cart",
                            color = Color.White,
                            fontSize = 14.sp,
                            fontWeight = FontWeight.Medium
                        )
                    }
                    else -> {
                        Text(
                            text = "Add To Cart",
                            color = Color.White,
                            fontSize = 14.sp,
                            fontWeight = FontWeight.Medium
                        )
                    }
                }
            }
        }
    }
}

// Additional UI Components
@Composable
fun LoadingSection() {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(200.dp),
        contentAlignment = Alignment.Center
    ) {
        CircularProgressIndicator(
            color = colorResource(R.color.primaryColor)
        )
    }
}

@Composable
fun LoadMoreSection() {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .padding(16.dp),
        contentAlignment = Alignment.Center
    ) {
        CircularProgressIndicator(
            color = colorResource(R.color.primaryColor),
            modifier = Modifier.size(24.dp)
        )
    }
}

@Composable
fun EmptyStateSection(
    searchQuery: String,
    selectedCategory: String?,
    onRefresh: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(32.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Icon(
            imageVector = Icons.Default.Search,
            contentDescription = "No products",
            modifier = Modifier.size(64.dp),
            tint = Color.Gray
        )

        Spacer(modifier = Modifier.height(16.dp))

        Text(
            text = when {
                searchQuery.isNotBlank() -> "No products found for '$searchQuery'"
                selectedCategory != null -> "No products found in $selectedCategory"
                else -> "No products available"
            },
            fontSize = 16.sp,
            color = Color.Gray,
            textAlign = TextAlign.Center
        )

        Spacer(modifier = Modifier.height(16.dp))

        Button(
            onClick = onRefresh,
            colors = ButtonDefaults.buttonColors(
                containerColor = colorResource(R.color.primaryColor)
            )
        ) {
            Text("Refresh")
        }
    }
}

@Composable
fun ErrorSection(
    error: String,
    onRetry: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(16.dp),
        colors = CardDefaults.cardColors(
            containerColor = Color.Red.copy(alpha = 0.1f)
        )
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = error,
                color = Color.Red,
                fontSize = 14.sp,
                textAlign = TextAlign.Center
            )

            Spacer(modifier = Modifier.height(8.dp))

            Button(
                onClick = onRetry,
                colors = ButtonDefaults.buttonColors(
                    containerColor = Color.Red
                )
            ) {
                Text("Retry", color = Color.White)
            }
        }
    }
}

val sampleProducts = listOf(
    Product(
        id = "1",
        name = "MB Fuel One Whey Protein Immunity+, 4.4 lb Chocolate",
        description = "Premium whey protein blend with immunity boosters, perfect for muscle building and recovery. Contains 25g protein per serving with added vitamins and minerals.",
        originalPrice = 5899,
        discountedPrice = 3549,
        discountPercentage = 30,
        rating = 4.5f,
        category = "Protein Powder",
        imageUrl = "https://example.com/images/mb-fuel-one.jpg",
        isFavorite = false,
        isInCart = false,
        isAddingToCart = false,
        stock = 25,
        brand = "MuscleBlaze",
        imageResId = "https://imgs.search.brave.com/YeL5eYtrZ1oUf7MtDdyRD_gPQoScEwVwClMB3IFKI1U/rs:fit:860:0:0:0/g:ce/aHR0cHM6Ly9pbWcz/LmhrcnRjZG4uY29t/LzI3NDYxL3ByZF8y/NzQ2MDcyLU1CLUZ1/ZWwtT25lLVdoZXkt/UHJvdGVpbi1JbW11/bml0eS00LjQtbGIt/Q2hvY29sYXRlX28u/anBn"
    ),
    Product(
        id = "2",
        name = "Healthkart HK Vitals ACV 750 mg Effervescent, 15 Tablets",
        description = "Apple Cider Vinegar effervescent tablets for weight management and digestive health. Easy to consume with natural apple flavor.",
        originalPrice = 350,
        discountedPrice = 299,
        discountPercentage = 15,
        rating = 4.2f,
        category = "Wellness",
        imageUrl = "https://example.com/images/hk-vitals-acv.jpg",
        isFavorite = true,
        isInCart = false,
        isAddingToCart = false,
        stock = 50,
        brand = "HealthKart",
        imageResId = "https://imgs.search.brave.com/Z2AjMvPYhrTlZalGozuo692-lRmkdpoLbiOwbii8BAI/rs:fit:860:0:0:0/g:ce/aHR0cHM6Ly9pbWcx/LmhrcnRjZG4uY29t/LzI0MDg5L3ByZF8y/NDA4ODQwLUhLLVZp/dGFscy1BQ1YtNzUw/LW1nLUVmZmVydmVz/Y2VudC1ieS1IZWFs/dGhLYXJ0LTE1LXRh/YmxldHMtV2F0ZXJt/ZWxvbl9vLmpwZw"

    ),
    Product(
        id = "3",
        name = "ON Gold Standard 100% Whey Protein Powder",
        description = "The world's best-selling whey protein powder. 24g of high-quality whey protein per serving with 5.5g naturally occurring BCAAs.",
        originalPrice = 6500,
        discountedPrice = 4999,
        discountPercentage = 23,
        rating = 4.7f,
        category = "Protein Powder",
        imageUrl = "https://example.com/images/on-gold-standard.jpg",
        isFavorite = false,
        isInCart = true,
        isAddingToCart = false,
        stock = 15,
        brand = "Optimum Nutrition",
        imageResId = "https://imgs.search.brave.com/TjQVMOocdEJdO4zxwebI6jTksPZ8YGIV9Cj0fFmZ7-g/rs:fit:860:0:0:0/g:ce/aHR0cHM6Ly9pNS53/YWxtYXJ0aW1hZ2Vz/LmNvbS9zZW8vT3B0/aW11bS1OdXRyaXRp/b24tR29sZC1TdGFu/ZGFyZC0xMDAtV2hl/eS1Qcm90ZWluLVBv/d2Rlci1Eb3VibGUt/UmljaC1DaG9jb2Fs/dGUtNS1sYi03NC1T/ZXJ2aW5nc180ZGMx/NGZjMS0zNGVhLTQw/M2QtOGQ5OS1mYjk5/ZjVlNjZhZDMuNmQx/ODRjOWZkZWI1YmJm/MzZlZTlmOTEzZmM3/NzkzNzIuanBlZw"

    ),
    Product(
        id = "4",
        name = "Muscletech Nitrotech Performance Series",
        description = "Advanced whey protein formula with creatine and amino acids. Scientifically engineered to build more muscle than regular whey protein.",
        originalPrice = 4200,
        discountedPrice = 3150,
        discountPercentage = 25,
        rating = 4.3f,
        category = "Protein Powder",
        imageUrl = "https://example.com/images/muscletech-nitrotech.jpg",
        isFavorite = true,
        isInCart = false,
        isAddingToCart = false,
        stock = 30,
        brand = "MuscleTech",
        imageResId = "https://imgs.search.brave.com/x5KdPbso7vkj8P2kt8HuEEyOVl6KspmJsszSxGKom-Y/rs:fit:860:0:0:0/g:ce/aHR0cHM6Ly9jZG4y/Lm51dHJhYmF5LmNv/bS91cGxvYWRzL3Zh/cmlhbnQvaW1hZ2Vz/L3ZhcmlhbnQtNTcw/My1mZWF0dXJlZF9p/bWFnZS1NdXNjbGVU/ZWNoX1BlcmZvcm1h/bmNlX1Nlcmllc19O/aXRyb1RlY2hfUmlw/cGVkX18wOV9LZ18x/OTlfTGJfQ2hvY29s/YXRlX0Z1ZGdlX0Jy/b3duaWUucG5n"
    ),
    Product(
        id = "5",
        name = "Dymatize ISO100 Hydrolyzed Protein Powder",
        description = "Fast-digesting, hydrolyzed whey protein isolate. 25g protein and 5.5g BCAAs per serving with zero grams of sugar and fat.",
        originalPrice = 7800,
        discountedPrice = 5999,
        discountPercentage = 23,
        rating = 4.6f,
        category = "Protein Powder",
        imageUrl = "https://example.com/images/dymatize-iso100.jpg",
        isFavorite = false,
        isInCart = false,
        isAddingToCart = false,
        stock = 20,
        brand = "Dymatize",
        imageResId = "https://imgs.search.brave.com/42Z2Wkyj9WKMstVedN-hW-4m7YBmYXl_UDx4XW-T1r0/rs:fit:860:0:0:0/g:ce/aHR0cHM6Ly9tLm1l/ZGlhLWFtYXpvbi5j/b20vaW1hZ2VzL0kv/NzEzZE1PRVQwRUwu/anBn"
    ),
    Product(
        id = "6",
        name = "BSN Syntha-6 Protein Powder",
        description = "Ultra-premium protein matrix with multiple protein sources. Designed for muscle recovery and growth with amazing taste.",
        originalPrice = 5500,
        discountedPrice = 4199,
        discountPercentage = 24,
        rating = 4.4f,
        category = "Protein Powder",
        imageUrl = "https://example.com/images/bsn-syntha6.jpg",
        isFavorite = false,
        isInCart = false,
        isAddingToCart = false,
        stock = 18,
        brand = "BSN",
        imageResId = "https://imgs.search.brave.com/bUKPjssl1nclctIogQ7c_1gIz3V57UQI0y34QFfsAKc/rs:fit:860:0:0:0/g:ce/aHR0cHM6Ly9ka3Mu/c2NlbmU3LmNvbS9p/cy9pbWFnZS9ka3Nj/ZG4vMThCU05VU1lO/VEg2UEJDSENHRU5f/aXM_d2lkPTI1MCZo/ZWk9MjUwJnFsdD04/NSwwJmZtdD1qcGcm/b3Bfc2hhcnBlbj0x"
    ),
    Product(
        id = "7",
        name = "Optimum Nutrition BCAA 1000 Capsules",
        description = "Branched Chain Amino Acids in convenient capsule form. Supports muscle recovery and reduces exercise-induced muscle fatigue.",
        originalPrice = 2500,
        discountedPrice = 1899,
        discountPercentage = 24,
        rating = 4.3f,
        category = "Amino Acids",
        imageUrl = "https://example.com/images/on-bcaa.jpg",
        isFavorite = false,
        isInCart = false,
        isAddingToCart = false,
        stock = 40,
        brand = "Optimum Nutrition",
        imageResId = "https://imgs.search.brave.com/PcpmqgMioDzw4Njgg198Zu_k2Xt13WIrNMfSeJxYVUU/rs:fit:860:0:0:0/g:ce/aHR0cHM6Ly9jbG91/ZGluYXJ5LmltYWdl/cy1paGVyYi5jb20v/aW1hZ2UvdXBsb2Fk/L2ZfYXV0byxxX2F1/dG86ZWNvL2ltYWdl/cy9vcG4vb3BuMDIw/MzUvci8zNy5qcGc"
    ),
    Product(
        id = "8",
        name = "HealthKart HK Vitals Multivitamin",
        description = "Complete multivitamin supplement with 25+ vitamins and minerals. Specially formulated for active individuals and athletes.",
        originalPrice = 1200,
        discountedPrice = 849,
        discountPercentage = 29,
        rating = 4.1f,
        category = "Vitamins",
        imageUrl = "https://example.com/images/hk-multivitamin.jpg",
        isFavorite = true,
        isInCart = false,
        isAddingToCart = false,
        stock = 60,
        brand = "HealthKart",
        imageResId = "https://imgs.search.brave.com/RrsuBBKC5_eYrA9uFYA_90ZWCeLN3isd_cyPUPSoFEk/rs:fit:860:0:0:0/g:ce/aHR0cHM6Ly9jZG4w/MS5waGFybWVhc3ku/aW4vZGFtL3Byb2R1/Y3RzX290Yy9XMzk2/MDQvaGVhbHRoa2Fy/dC1oay12aXRhbHMt/bXVsdGl2aXRhbWlu/LXdpdGgtbXVsdGlt/aW5lcmFsLXRhYmxl/dC10YXVyaW5lLWdp/bnNlbmctZXh0cmFj/dC05MC1jb3VudC0y/LTE3MjcxMzM1NTAu/anBnP2RpbT0xNDQw/eDE0NDAmcT03NQ"
    ),
    Product(
        id = "9",
        name = "Universal Nutrition Animal Pak",
        description = "The ultimate training pack with vitamins, minerals, amino acids, and antioxidants. Designed for serious athletes and bodybuilders.",
        originalPrice = 3800,
        discountedPrice = 2999,
        discountPercentage = 21,
        rating = 4.5f,
        category = "Vitamins",
        imageUrl = "https://example.com/images/animal-pak.jpg",
        isFavorite = false,
        isInCart = false,
        isAddingToCart = false,
        stock = 12,
        brand = "Universal Nutrition",
        imageResId = "https://imgs.search.brave.com/jfuSrYDlJy5nidYVF6YfLEXju1cAEfCu-BpVHJdhO8Y/rs:fit:860:0:0:0/g:ce/aHR0cHM6Ly9uZXRy/aXRpb24uY29tL2Nk/bi9zaG9wL2ZpbGVz/L0FuaW1hbFBBS180/NFBhY2tfQXVnMjAy/M18xXzJfODAweC5q/cGc_dj0xNzE0NDc1/ODY3"
    ),
    Product(
        id = "10",
        name = "Cellucor C4 Original Pre Workout",
        description = "Explosive pre-workout energy with beta-alanine, creatine nitrate, and caffeine. Perfect for intense training sessions.",
        originalPrice = 2800,
        discountedPrice = 2199,
        discountPercentage = 21,
        rating = 4.4f,
        category = "Pre Workout",
        imageUrl = "https://example.com/images/c4-original.jpg",
        isFavorite = false,
        isInCart = true,
        isAddingToCart = false,
        stock = 35,
        brand = "Cellucor",
        imageResId = "https://imgs.search.brave.com/OYnEA6YhNguMHDUc7Wr52HmcFjk3EVlvNQp-KUXE60g/rs:fit:860:0:0:0/g:ce/aHR0cHM6Ly93d3cu/ZGlzY291bnQtc3Vw/cGxlbWVudHMuY28u/dWsvY2RuL3Nob3Av/ZmlsZXMvY2VsbHVj/b3ItYzQtb3JpZ2lu/YWwtcHJlLXdvcmtv/dXQtNjAtc2Vydmlu/Z3MtMjEzNDc0Lmpw/Zz92PTE3MjA1MTgz/Mjgmd2lkdGg9e3dp/ZHRofQ"
    ),
    Product(
        id = "11",
        name = "MuscleBlaze CreaPro Creatine Monohydrate",
        description = "100% pure creatine monohydrate for enhanced strength, power, and muscle growth. Unflavored and easy to mix.",
        originalPrice = 1500,
        discountedPrice = 1049,
        discountPercentage = 30,
        rating = 4.2f,
        category = "Creatine",
        imageUrl = "https://example.com/images/mb-creapro.jpg",
        isFavorite = false,
        isInCart = false,
        isAddingToCart = false,
        stock = 45,
        brand = "MuscleBlaze",
        imageResId = "https://imgs.search.brave.com/OwXoTxG3Y6ikZOZr2fRB4iJE2t223PsTTeJNoSyYx4E/rs:fit:860:0:0:0/g:ce/aHR0cHM6Ly9tLm1l/ZGlhLWFtYXpvbi5j/b20vaW1hZ2VzL0kv/NDF2K1VRRFpqaEwu/anBn"
    ),
    Product(
        id = "12",
        name = "Scivation Xtend Original BCAA Powder",
        description = "Sugar-free BCAA powder with electrolytes. 7g BCAAs in the scientifically studied 2:1:1 ratio for optimal muscle recovery.",
        originalPrice = 3200,
        discountedPrice = 2499,
        discountPercentage = 22,
        rating = 4.6f,
        category = "Amino Acids",
        imageUrl = "https://example.com/images/xtend-bcaa.jpg",
        isFavorite = true,
        isInCart = false,
        isAddingToCart = false,
        stock = 28,
        brand = "Scivation",
        imageResId = "https://imgs.search.brave.com/ON70HL7p06lpekbXxWcWWPUxtMwv31zTQVMcn26u00I/rs:fit:860:0:0:0/g:ce/aHR0cHM6Ly93d3cu/dml0YW1hcnQuY2Ev/Y2RuL3Nob3AvcHJv/ZHVjdHMveHRlbmQz/MHNlcnZNYW5nb182/YWQ0OTgwNS1lNmNj/LTQ0ZWYtOGY1Zi1j/NzFkNTEwODk3MjIu/anBnP3Y9MTcxMDk3/MTUyMiZ3aWR0aD0x/NDQ1"
    ),
    Product(
        id = "13",
        name = "Now Sports Omega-3 Fish Oil",
        description = "High-potency omega-3 fatty acids from fish oil. Supports cardiovascular health and anti-inflammatory response.",
        originalPrice = 2200,
        discountedPrice = 1599,
        discountPercentage = 27,
        rating = 4.3f,
        category = "Wellness",
        imageUrl = "https://example.com/images/now-omega3.jpg",
        isFavorite = false,
        isInCart = false,
        isAddingToCart = false,
        stock = 55,
        brand = "Now Sports",
        imageResId = "https://imgs.search.brave.com/QSrGXWnlcvhE3JPvgTDI3NnzIGMskeYrlyuRFhUG7S0/rs:fit:860:0:0:0/g:ce/aHR0cHM6Ly9pbWFn/ZXMtbmEuc3NsLWlt/YWdlcy1hbWF6b24u/Y29tL2ltYWdlcy9J/LzcxVEtpbjJaQ2pM/LmpwZw"
    ),
    Product(
        id = "14",
        name = "Quest Nutrition Protein Bars Variety Pack",
        description = "High-protein, low-carb bars with fiber. Perfect on-the-go snack for fitness enthusiasts. Pack of 12 assorted flavors.",
        originalPrice = 2400,
        discountedPrice = 1899,
        discountPercentage = 21,
        rating = 4.1f,
        category = "Protein Bars",
        imageUrl = "https://example.com/images/quest-bars.jpg",
        isFavorite = false,
        isInCart = false,
        isAddingToCart = false,
        stock = 32,
        brand = "Quest Nutrition",
        imageResId = "https://imgs.search.brave.com/k78ahvJOAz5Ur5u1GcO26BdpsiMx3PO1KF64bZKHZ0U/rs:fit:860:0:0:0/g:ce/aHR0cHM6Ly9tLm1l/ZGlhLWFtYXpvbi5j/b20vaW1hZ2VzL0kv/ODFlVzhwMitaV0wu/anBn"
    ),
    Product(
        id = "15",
        name = "Gaspari Nutrition SuperPump Max Pre Workout",
        description = "Advanced pre-workout formula with nootropics and vasodilators. Delivers intense energy, focus, and pumps.",
        originalPrice = 3500,
        discountedPrice = 2799,
        discountPercentage = 20,
        rating = 4.5f,
        category = "Pre Workout",
        imageUrl = "https://example.com/images/superpump-max.jpg",
        isFavorite = false,
        isInCart = false,
        isAddingToCart = false,
        stock = 22,
        brand = "Gaspari Nutrition",
        imageResId = "https://imgs.search.brave.com/7pMWwposrGIglZkfW6cK7yeaXIxa-f2Km8KQ53oEeZc/rs:fit:860:0:0:0/g:ce/aHR0cHM6Ly9pLmVi/YXlpbWcuY29tL2lt/YWdlcy9nL2tkY0FB/T1N3Z2lKZlFoQVAv/cy1sNTAwLmpwZw"
    ),
    Product(
        id = "16",
        name = "HealthKart HK Vitals Vitamin D3",
        description = "High-potency Vitamin D3 (2000 IU) for bone health and immune support. Essential for calcium absorption.",
        originalPrice = 800,
        discountedPrice = 599,
        discountPercentage = 25,
        rating = 4.0f,
        category = "Vitamins",
        imageUrl = "https://example.com/images/hk-vitamin-d3.jpg",
        isFavorite = true,
        isInCart = false,
        isAddingToCart = false,
        stock = 75,
        brand = "HealthKart",
        imageResId = "https://imgs.search.brave.com/j8NC5T6f53stVXKPvqVwQHqsfEbESrdWvse4e39Jcls/rs:fit:860:0:0:0/g:ce/aHR0cHM6Ly9pLmVi/YXlpbWcuY29tL2lt/YWdlcy9nL0hKZ0FB/T1N3M3JCamdjVjQv/cy1sNTAwLmpwZw"
    ),
    Product(
        id = "17",
        name = "Nutrex Lipo 6 Black Ultra Concentrate",
        description = "Powerful fat burner with maximum potency formula. Supports metabolism and energy for effective weight management.",
        originalPrice = 4500,
        discountedPrice = 3499,
        discountPercentage = 22,
        rating = 4.2f,
        category = "Fat Burners",
        imageUrl = "https://example.com/images/lipo6-black.jpg",
        isFavorite = false,
        isInCart = false,
        isAddingToCart = false,
        stock = 18,
        brand = "Nutrex",
        imageResId = "https://imgs.search.brave.com/vNKs8JlJaHIYJcgXLj5h-9rHLo39XfILrf2YvGRmq-U/rs:fit:860:0:0:0/g:ce/aHR0cHM6Ly9tdXNj/bGVwb3dlci5wcm8v/MjAwMDctaG9tZV9k/ZWZhdWx0LzExMzEx/LU5VVFJFWC1MaXBv/LTYtQmxhY2stVWx0/cmEtQ29uY2VudHJh/dGUtRVUtNjBjYXBz/LmpwZw"
    ),
    Product(
        id = "18",
        name = "Serious Mass Weight Gainer",
        description = "High-calorie weight gain formula with 50g protein per serving. Perfect for hard gainers and mass building.",
        originalPrice = 8500,
        discountedPrice = 6799,
        discountPercentage = 20,
        rating = 4.3f,
        category = "Mass Gainers",
        imageUrl = "https://example.com/images/serious-mass.jpg",
        isFavorite = false,
        isInCart = false,
        isAddingToCart = false,
        stock = 14,
        brand = "Optimum Nutrition",
        imageResId = "https://imgs.search.brave.com/8DWAEjo1DMHkL9SMe7fUbJpTWdQP2gMqNRGv2K11Ncg/rs:fit:860:0:0:0/g:ce/aHR0cHM6Ly9jYW1w/dXNwcm90ZWluLmNv/bS9jZG4vc2hvcC9w/cm9kdWN0cy9WYW5p/bGxhLVNlcmlvdXMt/TWFzcy1XZWlnaHQt/R2FpbmVyLVByb3Rl/aW4tYnktT04tT3B0/aW11bS1OdXRyaXRp/b25fYTlhYzVhMWIt/NjdlMy00MDRkLTlm/YjUtMGQ5ZjA2MDVl/MTU3XzQwMHguanBn/P3Y9MTY3ODM2ODg2/Nw"
    ),
    Product(
        id = "19",
        name = "MusclePharm Combat 100% Casein",
        description = "Slow-digesting casein protein perfect for nighttime recovery. Provides sustained amino acid release for 8+ hours.",
        originalPrice = 5200,
        discountedPrice = 3999,
        discountPercentage = 23,
        rating = 4.4f,
        category = "Protein Powder",
        imageUrl = "https://example.com/images/combat-casein.jpg",
        isFavorite = false,
        isInCart = false,
        isAddingToCart = false,
        stock = 26,
        brand = "MusclePharm",
        imageResId = "https://imgs.search.brave.com/3_HiJ2xIpAZkayyLNg4WUs8zra58IUpHzGxfzwPIo10/rs:fit:860:0:0:0/g:ce/aHR0cHM6Ly9pNS53/YWxtYXJ0aW1hZ2Vz/LmNvbS9hc3IvYTJh/NTc0NjgtYmUxZi00/M2ZlLTg3ODItZjYy/OWQ1NjFhNTJiXzMu/M2ViYWE4ZTU4ODZm/ZDE2NDMzZDU5NjM0/NGYxODEwYzgucG5n/P29kbkhlaWdodD02/MTImb2RuV2lkdGg9/NjEyJm9kbkJnPUZG/RkZGRg"
    ),
    Product(
        id = "20",
        name = "Universal Nutrition Real Gains Weight Gainer",
        description = "Premium mass gainer with complex carbs and high-quality proteins. 50g protein and 600+ calories per serving.",
        originalPrice = 7800,
        discountedPrice = 5999,
        discountPercentage = 23,
        rating = 4.1f,
        category = "Mass Gainers",
        imageUrl = "https://example.com/images/real-gains.jpg",
        isFavorite = false,
        isInCart = false,
        isAddingToCart = false,
        stock = 16,
        brand = "Universal Nutrition",
        imageResId = "https://imgs.search.brave.com/Bvs9sQkV-w1bJhb2DbXmcZZxdzDOjHYJaURdkSu_Ml0/rs:fit:860:0:0:0/g:ce/aHR0cHM6Ly9jbG91/ZGluYXJ5LmltYWdl/cy1paGVyYi5jb20v/aW1hZ2UvdXBsb2Fk/L2ZfYXV0byxxX2F1/dG86ZWNvL2ltYWdl/cy91bm4vdW5uMDEy/MDIvdi8xMS5qcGc"
    )
)

// Categories for filtering
val productCategories = listOf(
    "All Products",
    "Protein Powder",
    "Pre Workout",
    "Amino Acids",
    "Vitamins",
    "Wellness",
    "Mass Gainers",
    "Fat Burners",
    "Protein Bars",
    "Creatine"
)

// Brands for filtering
val productBrands = listOf(
    "All Brands",
    "MuscleBlaze",
    "Optimum Nutrition",
    "HealthKart",
    "MuscleTech",
    "Dymatize",
    "BSN",
    "Universal Nutrition",
    "Cellucor",
    "Scivation",
    "Now Sports",
    "Quest Nutrition",
    "Gaspari Nutrition",
    "Nutrex",
    "MusclePharm"
)

@Preview
@Composable
private fun Preview() {
    NullpointerSquadTheme {
        ProductListScreen(
            state = ProductListState(),
            onAction = {}
        )
    }
}