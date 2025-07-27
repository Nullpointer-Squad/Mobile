package com.nullpointer.squad.presentation.productDetail.view

import android.content.ActivityNotFoundException
import android.content.Context
import android.content.Intent
import android.util.Log
import android.widget.Toast
import androidx.compose.animation.animateContentSize
import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.*
import androidx.compose.foundation.shape.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.snapshotFlow
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import coil.compose.AsyncImage
import coil.request.ImageRequest
import com.nullpointer.squad.BuildConfig
import com.nullpointer.squad.R
import com.nullpointer.squad.domain.model.ProductDetail
import com.nullpointer.squad.domain.model.ProductItem
import com.nullpointer.squad.presentation.productList.ProductListViewModel
import com.nullpointer.squad.presentation.productList.intent.ProductListAction
import com.nullpointer.squad.util.convertPrice
import kotlinx.coroutines.flow.distinctUntilChanged
import java.util.Locale

@Composable
fun ProductDetailRoot(
    viewModel: ProductListViewModel,
    productId: String,
    onNavigateBack: () -> Unit
) {
    val state by viewModel.state.collectAsStateWithLifecycle()
    val selectedCurrency by viewModel.selectedCurrency.collectAsState()

    Log.d("TAG", "ProductDetailRoot:${state.productDetailResponse?.data?.Stock}  ")

    LaunchedEffect(productId) {
        viewModel.onAction(ProductListAction.GetProductDetail(productId))
    }

    LaunchedEffect(state.productDetailResponse?.data?.Category) {
        state.productDetailResponse?.data?.Category?.let { category ->
            viewModel.onAction(ProductListAction.LoadSimilarProducts(category))
        }
    }

    val productDetail = state.productDetailResponse?.data
    val productFromList = state.products.data.products.find { it.Internal_id == productId }
    Log.d("TAG", "ProductDetailRoot1: ${productDetail?.Stock} ")
    if (state.isLoading && productDetail == null && productFromList == null) {
        LoadingView()
        return
    }

    ProductDetailScreen(
        product = productDetail,
        similarProducts = state.similarProducts.data.products,
        isLoadingSimilarProducts = state.isLoadingSimilar,
        onNavigateBack = onNavigateBack,
        currency = selectedCurrency,
        usdToInrRate = state.usdToInrRate,
        viewModel = viewModel,
        productId = productId
    )
}

@Composable
fun ProductDetailScreen(
    product: ProductDetail?,
    viewModel: ProductListViewModel,
    similarProducts: List<ProductItem>,
    onNavigateBack: () -> Unit,
    currency: String = "USD",
    usdToInrRate: Double = 83.0,
    isLoadingSimilarProducts: Boolean,
    productId: String
) {
    var selectedQuantity by remember { mutableIntStateOf(1) }

    if (product == null) {
        LoadingView()
        return
    }

    Scaffold(
        modifier = Modifier.background(Color.White),
        bottomBar = { BottomActionBar(product.Stock, product.Price, currency, usdToInrRate) }
    ) { paddingValues ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .background(Color.White)
        ) {
            item { TopBar(onNavigateBack) }
            item { ProductImage("${BuildConfig.IMAGE_BASE_URL}${product.Image}", product.Name, product.Internal_id) }
            item { ProductInfo(product, currency, usdToInrRate) }

            if (product.Stock > 0) {
                item {
                    QuantitySelection(
                        selectedQuantity,
                        { selectedQuantity = it },
                        minOf(product.Stock, 10)
                    )
                }
            }

            item { DeliveryServices() }
            item { ProductDetails(product.Description, product.Brand, product.Category) }
            item { CustomerReviews(product.Rating) }

            if (similarProducts.isNotEmpty()) {
                item {
                    SimilarProducts(
                        similarProducts,
                        productId,
                        isLoadingSimilarProducts,
                        currency,
                        usdToInrRate,
                        { viewModel.onAction(ProductListAction.NavigateToProductDetail(it)) },
                        viewModel
                    )
                }
            }

            item { Spacer(Modifier.height(16.dp)) }
        }
    }
}

@Composable
private fun TopBar(onNavigateBack: () -> Unit) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(16.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        IconButton(onClick = onNavigateBack) {
            Icon(Icons.AutoMirrored.Filled.ArrowBack, "Back", tint = Color.Black)
        }
        IconButton(onClick = {}) {
            Icon(Icons.Default.ShoppingCart, "Cart", tint = Color.Black)
        }
    }
}

@Composable
private fun ProductImage(imageUrl: String?, productName: String, productId: String) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(350.dp)
            .padding(16.dp),
        contentAlignment = Alignment.Center
    ) {
        Card(
            modifier = Modifier.fillMaxSize(),
            shape = RoundedCornerShape(12.dp),
            colors = CardDefaults.cardColors(Color.White),
            elevation = CardDefaults.cardElevation(2.dp)
        ) {
            AsyncImage(
                model = ImageRequest.Builder(LocalContext.current)
                    .data(imageUrl)
                    .crossfade(true)
                    .setHeader("User-Agent", "Mozilla/5.0")
                    .build(),
                placeholder = painterResource(R.drawable.ic_placeholder),
                error = painterResource(R.drawable.ic_placeholder),
                contentDescription = "Product Image of $productName",
                contentScale = ContentScale.Fit,
                modifier = Modifier
                    .fillMaxSize()
                    .padding(16.dp)
            )
        }

        val context = LocalContext.current
        Column(
            modifier = Modifier
                .align(Alignment.TopEnd)
                .padding(8.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            ActionButton(Icons.Default.FavoriteBorder, "Favorite") {}
            ActionButton(Icons.Default.Share, "Share") {
                shareProduct(context, productName, productId)
            }
        }
    }
}

@Composable
private fun ActionButton(icon: androidx.compose.ui.graphics.vector.ImageVector, contentDescription: String, onClick: () -> Unit) {
    IconButton(
        onClick = onClick,
        modifier = Modifier
            .background(Color.White, CircleShape)
            .size(40.dp)
    ) {
        Icon(icon, contentDescription, tint = Color.Gray, modifier = Modifier.size(20.dp))
    }
}

@Composable
private fun ProductInfo(
    product: ProductDetail,
    currency: String,
    usdToInrRate: Double
) {
    Column(Modifier
        .fillMaxWidth()
        .padding(horizontal = 16.dp)) {
        Text(product.Brand, fontSize = 14.sp, color = Color.Gray, fontWeight = FontWeight.Medium)
        Spacer(Modifier.height(4.dp))

        Text(
            product.Name,
            fontSize = 18.sp,
            fontWeight = FontWeight.Bold,
            color = Color.Black,
            lineHeight = 24.sp
        )

        if (product.ShortDescription.isNotBlank()) {
            Spacer(Modifier.height(8.dp))
            ExpandableText(product.ShortDescription)
        }

        Spacer(Modifier.height(12.dp))

        // Rating
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(4.dp),
                modifier = Modifier
                    .background(Color(0xFF4CAF50), RoundedCornerShape(4.dp))
                    .padding(horizontal = 8.dp, vertical = 4.dp)
            ) {
                Text(product.Rating.toString(), fontSize = 14.sp, fontWeight = FontWeight.Bold, color = Color.White)
                Icon(Icons.Filled.Star, "Star", tint = Color.White, modifier = Modifier.size(14.dp))
            }
            Text("(89 ratings)", fontSize = 12.sp, color = Color.Gray)
        }

        Spacer(Modifier.height(16.dp))

        // Price
        PriceSection(product.Price, product.InitialPrice, currency, usdToInrRate)

        Spacer(Modifier.height(8.dp))
        Text("Inclusive of all taxes", fontSize = 12.sp, color = Color.Gray)
        Spacer(Modifier.height(12.dp))

        // Stock
        Log.d("TAG", "ProductInfo: ${product.Stock}")
        StockStatus(product.Availability, product.Stock)
    }
}

@Composable
private fun PriceSection(finalPrice: Double, originalPrice: Double, currency: String, usdToInrRate: Double) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        val currencySymbol = if (currency == "USD") "$" else "₹"
        val displayPrice = finalPrice.convertPrice(currency, usdToInrRate)
        val displayOriginalPrice = originalPrice.convertPrice(currency, usdToInrRate)

        Text(
            "$currencySymbol${"%.2f".format(displayPrice)}",
            fontSize = 28.sp,
            fontWeight = FontWeight.Bold,
            color = Color.Black
        )

        if (originalPrice > finalPrice) {
            Text(
                "$currencySymbol${"%.2f".format(displayOriginalPrice)}",
                fontSize = 18.sp,
                color = Color.Gray,
                textDecoration = TextDecoration.LineThrough
            )

            val discountPercentage = ((originalPrice - finalPrice) / originalPrice * 100).toInt()
            Text(
                "$discountPercentage% OFF",
                fontSize = 14.sp,
                color = Color.White,
                fontWeight = FontWeight.Bold,
                modifier = Modifier
                    .background(Color(0xFFFF5722), RoundedCornerShape(4.dp))
                    .padding(horizontal = 8.dp, vertical = 4.dp)
            )
        }
    }
}

@Composable
private fun StockStatus(isAvailable: Boolean, stock: Int) {
    Column(
        verticalArrangement = Arrangement.spacedBy(4.dp)
    ) {
        // Stock count display
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            Icon(
                Icons.Default.Inventory,
                "Stock Count",
                tint = Color.Gray,
                modifier = Modifier.size(16.dp)
            )
            Text(
                "Stock: $stock units",
                fontSize = 14.sp,
                color = Color.Gray,
                fontWeight = FontWeight.Medium
            )
        }

        // Stock status display
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            val stockColor = when {
                stock == 0 -> Color.Red
                stock < 10 -> Color(0xFFFF9800)
                else -> Color(0xFF4CAF50)
            }

            val stockText = when {
                stock == 0 -> "Out of Stock"
                !isAvailable && stock > 0 -> "Currently Unavailable"
                stock < 10 -> "Only $stock left in stock"
                else -> "In Stock"
            }

            Icon(Icons.Default.Info, "Stock Info", tint = stockColor, modifier = Modifier.size(16.dp))
            Text(stockText, fontSize = 14.sp, color = stockColor, fontWeight = FontWeight.Medium)
        }
    }
}

@Composable
private fun ExpandableText(text: String, minimizedMaxLines: Int = 2) {
    var isExpanded by remember { mutableStateOf(false) }

    Text(
        text = text,
        fontSize = 14.sp,
        color = Color(0xFF666666),
        lineHeight = 18.sp,
        maxLines = if (isExpanded) Int.MAX_VALUE else minimizedMaxLines,
        overflow = TextOverflow.Ellipsis,
        modifier = Modifier
            .clickable { isExpanded = !isExpanded }
            .animateContentSize()
    )
}

@Composable
private fun QuantitySelection(quantity: Int, onQuantityChanged: (Int) -> Unit, maxQuantity: Int) {
    Column(Modifier
        .fillMaxWidth()
        .padding(horizontal = 16.dp, vertical = 12.dp)) {
        Text("Quantity", fontSize = 16.sp, fontWeight = FontWeight.Medium, color = Color.Black)
        Spacer(Modifier.height(8.dp))

        Row(
            modifier = Modifier
                .fillMaxWidth()
                .background(Color.Gray.copy(0.1f), RoundedCornerShape(8.dp))
                .padding(12.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                QuantityButton("-", quantity > 1) { if (quantity > 1) onQuantityChanged(quantity - 1) }
                Text(quantity.toString(), fontSize = 16.sp, fontWeight = FontWeight.Bold, color = Color.Black)
                QuantityButton("+", quantity < maxQuantity) { if (quantity < maxQuantity) onQuantityChanged(quantity + 1) }
            }
            Text("Max: $maxQuantity", fontSize = 12.sp, color = Color.Gray)
        }
    }
}

@Composable
private fun QuantityButton(text: String, enabled: Boolean, onClick: () -> Unit) {
    IconButton(
        onClick = onClick,
        modifier = Modifier
            .background(Color.White, RoundedCornerShape(4.dp))
            .size(32.dp)
    ) {
        Text(
            text = text,
            fontSize = 18.sp,
            fontWeight = FontWeight.Bold,
            color = if (enabled) Color.Black else Color.Gray
        )
    }
}

@Composable
private fun DeliveryServices() {
    Column(Modifier
        .fillMaxWidth()
        .padding(horizontal = 16.dp, vertical = 8.dp)) {
        DeliveryServiceItem(Icons.Default.LocalShipping, "FREE Delivery on orders above ₹499")
        DeliveryServiceItem(Icons.Default.Payments, "Cash on Delivery available")
        DeliveryServiceItem(Icons.Default.Replay, "7 days return & exchange")
    }
}

@Composable
private fun DeliveryServiceItem(icon: androidx.compose.ui.graphics.vector.ImageVector, text: String) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(8.dp),
        modifier = Modifier.padding(vertical = 4.dp)
    ) {
        Icon(icon, text, tint = Color(0xFF4CAF50), modifier = Modifier.size(20.dp))
        Text(text, fontSize = 14.sp, color = Color.Black)
    }
}

@Composable
private fun ProductDetails(description: String, brand: String, category: String?) {
    var isDescriptionExpanded by remember { mutableStateOf(false) }
    var isSpecsExpanded by remember { mutableStateOf(false) }

    Column(Modifier
        .fillMaxWidth()
        .padding(horizontal = 16.dp, vertical = 12.dp)) {
        Text("Product Details", fontSize = 16.sp, fontWeight = FontWeight.Medium, color = Color.Black)
        Spacer(Modifier.height(12.dp))

        ExpandableSection("Description", isDescriptionExpanded, { isDescriptionExpanded = !isDescriptionExpanded }) {
            Text(description, fontSize = 14.sp, color = Color(0xFF666666), lineHeight = 20.sp)
        }

        Spacer(Modifier.height(8.dp))

        ExpandableSection("Specifications", isSpecsExpanded, { isSpecsExpanded = !isSpecsExpanded }) {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                SpecRow("Brand", brand)
                SpecRow("Category", category ?: "")
                SpecRow("Type", "Safety Vest")
                SpecRow("Material", "100% Ultra-Cool polyester mesh")
                SpecRow("Compliance", "ANSI Type R / Class 2")
            }
        }
    }
}

@Composable
private fun SpecRow(label: String, value: String) {
    Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
        Text(label, fontSize = 14.sp, color = Color.Gray, modifier = Modifier.weight(1f))
        Text(value, fontSize = 14.sp, color = Color.Black, fontWeight = FontWeight.Medium, modifier = Modifier.weight(2f))
    }
}

@Composable
private fun ExpandableSection(
    title: String,
    isExpanded: Boolean,
    onToggle: () -> Unit,
    content: @Composable () -> Unit
) {
    Column {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .clickable { onToggle() }
                .padding(vertical = 8.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(title, fontSize = 14.sp, fontWeight = FontWeight.Medium, color = Color.Black)
            Icon(
                if (isExpanded) Icons.Default.KeyboardArrowUp else Icons.Default.KeyboardArrowDown,
                if (isExpanded) "Collapse" else "Expand",
                tint = Color.Gray
            )
        }

        if (isExpanded) {
            Spacer(Modifier.height(8.dp))
            content()
        }

        HorizontalDivider(
            modifier = Modifier.padding(top = 8.dp),
            thickness = DividerDefaults.Thickness,
            color = Color.Gray.copy(0.2f)
        )
    }
}

@Composable
private fun CustomerReviews(rating: Double) {
    val distribution = remember(rating) { getAssumedRatingDistribution(rating) }

    Column(Modifier
        .fillMaxWidth()
        .padding(horizontal = 16.dp, vertical = 12.dp)) {
        Text("Customer Reviews", fontSize = 16.sp, fontWeight = FontWeight.Medium)
        Spacer(Modifier.height(16.dp))

        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(8.dp),
            colors = CardDefaults.cardColors(Color(0xFFF5F5F5))
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally, modifier = Modifier.weight(1f)) {
                    Text(String.format(Locale.US,"%.1f", rating), fontSize = 36.sp, fontWeight = FontWeight.Bold)
                    Row {
                        repeat(5) { index ->
                            Icon(
                                if (index < rating.toInt()) Icons.Filled.Star else Icons.Default.StarBorder,
                                "Star",
                                tint = Color(0xFFFFC107),
                                modifier = Modifier.size(16.dp)
                            )
                        }
                    }
                }

                Column(modifier = Modifier.weight(2f), verticalArrangement = Arrangement.spacedBy(4.dp)) {
                    distribution.forEach { (stars, percentage) ->
                        RatingBar(stars, percentage)
                    }
                }
            }
        }
    }
}

@Composable
private fun RatingBar(stars: Int, percentage: Float) {
    Row(verticalAlignment = Alignment.CenterVertically) {
        Text("$stars", fontSize = 12.sp, modifier = Modifier.width(20.dp))
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(8.dp)
                .padding(horizontal = 8.dp)
                .background(Color.LightGray, RoundedCornerShape(50.dp))
        ) {
            Box(
                modifier = Modifier
                    .fillMaxHeight()
                    .fillMaxWidth(percentage)
                    .background(Color(0xFFFFC107))
            )
        }
    }
}

@Composable
private fun SimilarProducts(
    products: List<ProductItem>,
    currentProductId: String,
    isLoading: Boolean,
    currency: String,
    usdToInrRate: Double,
    onProductClick: (String) -> Unit,
    viewModel: ProductListViewModel
) {
    val listState = rememberLazyListState()
    val filteredProducts = remember(products, currentProductId) {
        products.filter { it.Internal_id != currentProductId }
    }

    LaunchedEffect(listState, filteredProducts.size) {
        snapshotFlow { listState.layoutInfo.visibleItemsInfo.lastOrNull()?.index }
            .distinctUntilChanged()
            .collect { lastVisibleIndex ->
                if (lastVisibleIndex != null && !isLoading && filteredProducts.isNotEmpty()) {
                    val totalItemsCount = filteredProducts.size
                    if (lastVisibleIndex >= totalItemsCount - 3) {
                        viewModel.onAction(ProductListAction.LoadMoreSimilarProducts)
                    }
                }
            }
    }

    Column(Modifier
        .fillMaxWidth()
        .padding(vertical = 12.dp)) {
        Text(
            "Similar Products",
            fontSize = 16.sp,
            fontWeight = FontWeight.Medium,
            color = Color.Black,
            modifier = Modifier.padding(horizontal = 16.dp)
        )
        Spacer(Modifier.height(12.dp))

        when {
            isLoading && filteredProducts.isEmpty() -> {
                Box(Modifier
                    .fillMaxWidth()
                    .height(150.dp), contentAlignment = Alignment.Center) {
                    CircularProgressIndicator(color = Color(0xFF4A90E2), modifier = Modifier.size(24.dp))
                }
            }
            filteredProducts.isNotEmpty() -> {
                LazyRow(
                    state = listState,
                    horizontalArrangement = Arrangement.spacedBy(12.dp),
                    contentPadding = PaddingValues(horizontal = 16.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    items(filteredProducts.size) { index ->
                        SimilarProductCard(
                            filteredProducts[index],
                            currency,
                            usdToInrRate
                        ) { onProductClick(filteredProducts[index].Internal_id) }
                    }
                }
            }
            else -> {
                Box(Modifier
                    .fillMaxWidth()
                    .height(100.dp), contentAlignment = Alignment.Center) {
                    Text("No similar products found", fontSize = 14.sp, color = Color.Gray, textAlign = TextAlign.Center)
                }
            }
        }
    }
}

@Composable
private fun SimilarProductCard(
    product: ProductItem,
    currency: String,
    usdToInrRate: Double,
    onClick: () -> Unit
) {
    Card(
        modifier = Modifier
            .width(150.dp)
            .height(210.dp)
            .clickable { onClick() },
        shape = RoundedCornerShape(8.dp),
        colors = CardDefaults.cardColors(
            containerColor = Color.White
        ),
        elevation = CardDefaults.cardElevation(2.dp)
    ) {
        Column(Modifier
            .fillMaxWidth()
            .background(Color.White)
            .padding(8.dp)) {
            AsyncImage(
                model = ImageRequest.Builder(LocalContext.current)
                    .data("${BuildConfig.IMAGE_BASE_URL}${product.Image}")
                    .crossfade(true)
                    .setHeader("User-Agent", "Mozilla/5.0")
                    .build(),
                placeholder = painterResource(R.drawable.ic_placeholder),
                error = painterResource(R.drawable.ic_placeholder),
                contentDescription = "Product",
                modifier = Modifier
                    .fillMaxWidth()
                    .height(100.dp)
                    .clip(RoundedCornerShape(6.dp)),
                contentScale = ContentScale.Fit
            )

            Spacer(Modifier.height(8.dp))

            Text(
                product.Name,
                fontSize = 12.sp,
                fontWeight = FontWeight.Medium,
                color = Color.Black,
                maxLines = 2,
                overflow = TextOverflow.Ellipsis,
                lineHeight = 16.sp
            )

            Spacer(Modifier.height(4.dp))

            val currencySymbol = if (currency == "USD") "$" else "₹"
            val displayPrice = product.Price.convertPrice(currency, usdToInrRate)

            Text(
                "$currencySymbol${"%.2f".format(displayPrice)}",
                fontSize = 14.sp,
                fontWeight = FontWeight.Bold,
                color = Color.Black
            )

            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(2.dp),
                modifier = Modifier.padding(top = 4.dp)
            ) {
                Icon(Icons.Filled.Star, "Star", tint = Color(0xFFFFC107), modifier = Modifier.size(12.dp))
                Text(product.Rating.toString(), fontSize = 11.sp, color = Color.Black)
            }
        }
    }
}

@Composable
private fun BottomActionBar(stock: Int, price: Double, currency: String, usdToInrRate: Double) {
    Surface(
        modifier = Modifier.fillMaxWidth(),
        shadowElevation = 8.dp,
        color = Color.White
    ) {
        Column {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                horizontalArrangement = Arrangement.spacedBy(12.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text("Total Price", fontSize = 12.sp, color = Color.Gray)

                    val currencySymbol = if (currency == "USD") "$" else "₹"
                    val displayPrice = price.convertPrice(currency, usdToInrRate)

                    Text(
                        "$currencySymbol${"%.2f".format(displayPrice)}",
                        fontSize = 20.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color.Black
                    )
                }
                Button(
                    modifier = Modifier.weight(1.5f),
                    colors = ButtonDefaults.buttonColors(
                        if (stock == 0) Color.Gray.copy(0.6f) else Color(0xFFFF5722)
                    ),
                    shape = RoundedCornerShape(8.dp),
                    onClick = {}
                ) {
                    if (stock == 0) {
                        Text("Out of Stock", color = Color.White, fontSize = 16.sp, fontWeight = FontWeight.Medium)
                    } else {
                        Icon(Icons.Default.ShoppingCart, null, modifier = Modifier.size(16.dp), tint = Color.White)
                        Spacer(Modifier.width(4.dp))
                        Text("Add to Cart", color = Color.White, fontSize = 16.sp, fontWeight = FontWeight.Medium)
                    }
                }
            }
            Spacer(Modifier.height(12.dp))
        }
    }
}

@Composable
private fun LoadingView() {
    Box(Modifier
        .fillMaxSize()
        .background(Color.White), contentAlignment = Alignment.Center) {
        CircularProgressIndicator(color = colorResource(R.color.buttonColorr))
    }
}

// Helper functions
private fun getAssumedRatingDistribution(average: Double): List<Pair<Int, Float>> {
    return when {
        average >= 4.8 -> listOf(5 to 0.85f, 4 to 0.10f, 3 to 0.03f, 2 to 0.01f, 1 to 0.01f)
        average >= 4.5 -> listOf(5 to 0.75f, 4 to 0.15f, 3 to 0.06f, 2 to 0.02f, 1 to 0.02f)
        average >= 4.0 -> listOf(5 to 0.60f, 4 to 0.25f, 3 to 0.10f, 2 to 0.03f, 1 to 0.02f)
        average >= 3.5 -> listOf(5 to 0.40f, 4 to 0.30f, 3 to 0.20f, 2 to 0.05f, 1 to 0.05f)
        else -> listOf(5 to 0.25f, 4 to 0.25f, 3 to 0.20f, 2 to 0.15f, 1 to 0.15f)
    }
}

private fun shareProduct(context: Context, productName: String, productId: String) {
    val shareIntent = Intent().apply {
        action = Intent.ACTION_SEND
        type = "text/plain"
        putExtra(
            Intent.EXTRA_TEXT,
            "Check out this amazing product: $productName\n\n" +
                    "View product: ${BuildConfig.BASE_URL}products/$productId\n\n" +
                    "Download our app to get exclusive deals!"
        )
        putExtra(Intent.EXTRA_SUBJECT, productName)
    }

    val chooserIntent = Intent.createChooser(shareIntent, "Share Product")

    try {
        context.startActivity(chooserIntent)
    } catch (_: ActivityNotFoundException) {
        Toast.makeText(context, "No app found to share", Toast.LENGTH_SHORT).show()
    }
}