package com.nullpointer.squad.presentation.productList.view

import android.util.Log
import androidx.compose.animation.Crossfade
import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.*
import androidx.compose.foundation.shape.*
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.*
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.RectangleShape
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardCapitalization
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import coil.compose.AsyncImagePainter
import coil.compose.SubcomposeAsyncImage
import coil.compose.SubcomposeAsyncImageContent
import coil.request.ImageRequest
import com.nullpointer.squad.BuildConfig
import com.nullpointer.squad.R
import com.nullpointer.squad.domain.model.LatestProduct
import com.nullpointer.squad.domain.model.ProductItem
import com.nullpointer.squad.presentation.productList.ProductListViewModel
import com.nullpointer.squad.presentation.productList.intent.ProductListAction
import com.nullpointer.squad.presentation.productList.model.ProductListState
import com.nullpointer.squad.util.ShimmerBox
import com.nullpointer.squad.util.convertPrice
import com.nullpointer.squad.widget.NpsScaffold
import kotlinx.coroutines.delay

@Composable
fun ProductListScreen(
    viewModel: ProductListViewModel,
    onAction: (ProductListAction) -> Unit,
) {
    var searchQuery by remember { mutableStateOf("") }
    val selectedCurrency by viewModel.selectedCurrency.collectAsState()
    val state by viewModel.state.collectAsStateWithLifecycle()
    var selectedSortOption by rememberSaveable { mutableStateOf("") }
    val listState = rememberLazyListState()

    // Handle state changes
    LaunchedEffect(state.currentSortField, state.isSorted) {
        selectedSortOption = if (state.isSorted) state.currentSortField else ""
    }

    LaunchedEffect(state.currentPage, state.isSorted, state.currentSortField, state.searchQuery) {
        if (state.currentPage > 0 || state.isSorted || state.searchQuery.isEmpty()) {
            listState.animateScrollToItem(0)
        }
    }

    state.message?.let { LaunchedEffect(it) { viewModel.clearMessage() } }
    state.error?.let { LaunchedEffect(it) { viewModel.clearError() } }

    NpsScaffold(
        topAppBar = {
            TopAppBar(searchQuery, { searchQuery = it; onAction(ProductListAction.SearchProducts(it)) }, onAction)
        },
        content = {paddingValues ->
            Box(Modifier.fillMaxSize()) {
                LazyColumn(
                    state = listState,
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(paddingValues),
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    // Banner (only on first page, not searching/sorted)
                    if (state.searchQuery.isEmpty() && !state.isSorted && state.currentPage == 1) {
                        item { HeroBanner(listOf(R.drawable.banner1, R.drawable.banner2, R.drawable.banner3)) }
                    }

                    // Sort dropdown
                    if (state.searchQuery.isEmpty()) {
                        item {
                            SortDropdown(selectedSortOption, state.isSorted, state.isLoading) { option ->
                                selectedSortOption = if (option == "Clear Sort") "" else option
                                onAction(ProductListAction.SortBy(option))
                            }
                        }
                    }

                    // Latest products
                    if (state.searchQuery.isEmpty() && !state.isSorted && state.currentPage == 1 && state.latestProducts.data.isNotEmpty()) {
                        item { LatestProducts(state.latestProducts.data, onAction, selectedCurrency, state.usdToInrRate) }
                    }

                    // Section header
                    item {
                        SectionHeader(
                            title = getSectionTitle(state),
                            actionText = getActionText(state),
                            onActionClick = {
                                when {
                                    state.searchQuery.isNotEmpty() -> {
                                        searchQuery = ""
                                        onAction(ProductListAction.ViewAllProducts)
                                    }
                                    state.isSorted -> {
                                        selectedSortOption = ""
                                        onAction(ProductListAction.SortBy("Clear Sort"))
                                    }
                                    else -> onAction(ProductListAction.ViewAllProducts)
                                }
                            }
                        )
                    }

                    // Content
                    when {
                        state.isLoading && state.products.data.products.isEmpty() -> item { LoadingView() }
                        state.products.data.products.isNotEmpty() -> {
                            items(state.products.data.products.chunked(2)) { productPair ->
                                ProductRow(productPair, onAction, selectedCurrency, state)
                            }
                            if (state.searchQuery.isEmpty() && state.totalPages > 1) {
                                item { Pagination(state.currentPage, state.totalPages, { onAction(ProductListAction.NavigateToPage(it)) }, state.isSorted, state.currentSortField, state.isLoading) }
                            }
                        }
                        !state.isLoading -> item { EmptyState(state, searchQuery) { onAction(getRefreshAction(state, searchQuery)) } }
                    }

                    item { Spacer(Modifier.size(10.dp)) }
                }

                state.error?.let { ErrorOverlay(it) { onAction(ProductListAction.RefreshProducts) } }
            }
        }
    )
}

@Composable
private fun TopAppBar(
    searchQuery: String,
    onSearchChange: (String) -> Unit,
    onAction: (ProductListAction) -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .background(colorResource(R.color.primaryColor))
            .padding(16.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            OutlinedTextField(
                value = searchQuery,
                onValueChange = onSearchChange,
                placeholder = { Text("Enter index number to search", color = Color.Gray) },
                leadingIcon = { Icon(Icons.Default.Search, "Search", tint = Color.Gray) },
                modifier = Modifier.weight(1f),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = Color.Transparent,
                    unfocusedBorderColor = Color.Transparent,
                    focusedContainerColor = Color.White,
                    unfocusedContainerColor = Color.White
                ),
                shape = RoundedCornerShape(8.dp),
                keyboardOptions = KeyboardOptions(capitalization = KeyboardCapitalization.None,
                    autoCorrectEnabled = false,
                    keyboardType = KeyboardType.Number,
                    imeAction = ImeAction.Search
                ),
                keyboardActions = KeyboardActions(
                    onSearch = { onAction(ProductListAction.SearchProducts(searchQuery)) }
                ),
                singleLine = true
            )


            IconButton(onClick = { onAction(ProductListAction.ToggleCurrency) }) {
                Icon(Icons.Default.CurrencyExchange, "Currency", tint = Color.White)
            }
        }
    }
}

@Composable
private fun HeroBanner(imageResList: List<Int>, height: Dp = 200.dp) {
    var currentIndex by remember { mutableStateOf(0) }

    LaunchedEffect(Unit) {
        while (true) {
            delay(3000L)
            currentIndex = (currentIndex + 1) % imageResList.size
        }
    }

    Column(Modifier.fillMaxWidth(), horizontalAlignment = Alignment.CenterHorizontally) {
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .height(height),
            shape = RectangleShape,
            elevation = CardDefaults.cardElevation(4.dp)
        ) {
            Crossfade(currentIndex, label = "Banner") { index ->
                Image(
                    painterResource(imageResList[index]),
                    "Banner $index",
                    contentScale = ContentScale.Crop,
                    modifier = Modifier.fillMaxSize()
                )
            }
        }

        Spacer(Modifier.height(12.dp))

        Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
            imageResList.forEachIndexed { index, _ ->
                Box(
                    Modifier
                        .size(10.dp)
                        .clip(CircleShape)
                        .background(if (index == currentIndex) colorResource(R.color.primaryColor) else Color.Transparent)
                        .border(1.dp, colorResource(R.color.primaryColor), CircleShape)
                )
            }
        }
    }
}

@Composable
private fun SortDropdown(
    selectedOption: String,
    isSorted: Boolean,
    isLoading: Boolean,
    onSortSelected: (String) -> Unit
) {
    var expanded by remember { mutableStateOf(false) }
    val sortOptions = listOf("Clear Sort", "Name", "Price", "Stock")

    Box(Modifier
        .wrapContentSize(Alignment.TopStart)
        .padding(start = 16.dp)) {
        OutlinedButton(
            onClick = { expanded = true },
            enabled = !isLoading,
            border = BorderStroke(1.dp, if (isSorted) colorResource(R.color.primaryColor) else Color.Gray),
            shape = RoundedCornerShape(8.dp),
            colors = ButtonDefaults.outlinedButtonColors(
                contentColor = if (isSorted) colorResource(R.color.primaryColor) else Color.Black
            )
        ) {
            if (isLoading) {
                CircularProgressIndicator(Modifier.size(16.dp), strokeWidth = 2.dp, color = colorResource(R.color.primaryColor))
                Spacer(Modifier.width(8.dp))
            }

            Text(
                when {
                    isLoading -> "Sorting..."
                    selectedOption.isEmpty() -> "Sort by"
                    else -> selectedOption
                },
                fontSize = 14.sp,
                fontWeight = if (isSorted) FontWeight.Medium else FontWeight.Normal
            )

            if (!isLoading) Icon(Icons.Default.ArrowDropDown, null)
        }

        DropdownMenu(expanded, { expanded = false }) {
            sortOptions.forEach { label ->
                DropdownMenuItem(
                    onClick = { expanded = false; onSortSelected(label) },
                    text = {
                        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            Text(
                                label,
                                color = when {
                                    label == "Clear Sort" && !isSorted -> Color.Gray
                                    label == selectedOption -> colorResource(R.color.primaryColor)
                                    else -> Color.Black
                                },
                                fontWeight = if (label == selectedOption) FontWeight.Medium else FontWeight.Normal
                            )
                            if (label == selectedOption && isSorted) {
                                Icon(Icons.Default.Check, "Selected", tint = colorResource(R.color.primaryColor), modifier = Modifier.size(16.dp))
                            }
                        }
                    },
                    enabled = !(label == "Clear Sort" && !isSorted)
                )
            }
        }
    }
}

@Composable
private fun SectionHeader(title: String, actionText: String, onActionClick: () -> Unit) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(title, fontSize = 18.sp, fontWeight = FontWeight.Bold, color = Color.Black)
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(4.dp),
            modifier = Modifier.clickable { onActionClick() }
        ) {
            Text(actionText, fontSize = 14.sp, color = Color.Gray)
            Icon(Icons.AutoMirrored.Filled.ArrowForward, "Arrow", tint = Color.Gray, modifier = Modifier.size(16.dp))
        }
    }
}

@Composable
private fun LatestProducts(
    products: List<LatestProduct>,
    onAction: (ProductListAction) -> Unit,
    currency: String,
    usdToInrRate: Double
) {
    Column(Modifier.fillMaxWidth()) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text("Latest Products", fontSize = 18.sp, fontWeight = FontWeight.Bold, color = Color.Black)
            Text(
                "NEW",
                fontSize = 10.sp,
                fontWeight = FontWeight.Bold,
                color = Color.White,
                modifier = Modifier
                    .background(Color(0xFF4CAF50), RoundedCornerShape(4.dp))
                    .padding(horizontal = 6.dp, vertical = 2.dp)
            )
        }

        Spacer(Modifier.height(12.dp))

        LazyRow(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(12.dp),
            contentPadding = PaddingValues(horizontal = 16.dp)
        ) {
            items(products) { product ->
                LatestProductCard(product, onAction, currency, usdToInrRate)
            }
        }
    }
}

@Composable
private fun LatestProductCard(
    product: LatestProduct,
    onAction: (ProductListAction) -> Unit,
    currency: String,
    usdToInrRate: Double
) {
    Card(
        modifier = Modifier
            .width(280.dp)
            .height(170.dp)
            .clickable { onAction(ProductListAction.NavigateToProductDetail(product.Internal_id)) },
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(
            containerColor = Color.White
        ),
        elevation = CardDefaults.cardElevation(4.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .background(Color.White)
                .padding(12.dp)
        ) {
            ProductImage("${BuildConfig.IMAGE_BASE_URL}${product.Image}", Modifier
                .size(80.dp)
                .clip(RoundedCornerShape(8.dp)))

            Spacer(Modifier.width(12.dp))

            Column(
                modifier = Modifier.fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                Text(product.Name, fontSize = 14.sp, fontWeight = FontWeight.Bold, color = Color.Black, maxLines = 2, overflow = TextOverflow.Ellipsis, lineHeight = 16.sp)
                Text(product.Brand, fontSize = 12.sp, color = Color.Gray, fontWeight = FontWeight.Medium, maxLines = 1, overflow = TextOverflow.Ellipsis)

                PriceSection(product.Price, product.InitialPrice, currency, usdToInrRate)
                RatingSection(product.Rating)
            }
        }
    }
}

@Composable
private fun ProductRow(
    products: List<ProductItem>,
    onAction: (ProductListAction) -> Unit,
    currency: String,
    state: ProductListState
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp),
        horizontalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        products.forEach { product ->
            ProductCard(product, Modifier.weight(1f), onAction, currency, state.usdToInrRate)
        }
        if (products.size == 1) Spacer(Modifier.weight(1f))
    }
}

@Composable
private fun ProductCard(
    product: ProductItem,
    modifier: Modifier,
    onAction: (ProductListAction) -> Unit,
    currency: String,
    usdToInrRate: Double
) {
    Card(
        modifier = modifier
            .height(250.dp)
            .clickable { onAction(ProductListAction.NavigateToProductDetail(product.Internal_id)) },
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(Color.White),
        elevation = CardDefaults.cardElevation(2.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .wrapContentHeight()
                .background(Color.White)
                .padding(12.dp)
        ) {
            ProductImage("${BuildConfig.IMAGE_BASE_URL}${product.Image}", Modifier
                .fillMaxWidth()
                .height(100.dp)
                .clip(RoundedCornerShape(8.dp)))

            Spacer(Modifier.height(8.dp))

            Text(product.Name, fontSize = 12.sp, fontWeight = FontWeight.Medium, color = Color.Black, maxLines = 2, overflow = TextOverflow.Ellipsis, lineHeight = 15.sp)

            Spacer(Modifier.height(8.dp))

            Row(verticalAlignment = Alignment.Top, modifier = Modifier.fillMaxWidth()) {
                val currencySymbol = if (currency == "USD") "$" else "₹"
                val convertedPrice = product.Price.convertPrice(currency, usdToInrRate)
                val convertedInitialPrice = product.InitialPrice.convertPrice(currency, usdToInrRate)

                Column(modifier = Modifier.height(IntrinsicSize.Min), horizontalAlignment = Alignment.Start) {
                    Text("$currencySymbol${"%.2f".format(convertedPrice)}", fontSize = 14.sp, fontWeight = FontWeight.Bold, color = Color.Black)
                    if (product.InitialPrice.toInt() != 0) {
                        Text("$currencySymbol${"%.2f".format(convertedInitialPrice)}", fontSize = 12.sp, color = Color.Gray, textDecoration = TextDecoration.LineThrough)
                    }
                }

                Spacer(Modifier.weight(1f))

                if (product.InitialPrice.toInt() != 0) {
                    val discountPercentage = ((product.InitialPrice - product.Price) / product.Price * 100).toInt()
                    Text("${discountPercentage}% Off", fontSize = 8.sp, color = Color(0xFF4CAF50), fontWeight = FontWeight.Medium)
                }
            }

            Spacer(Modifier.height(6.dp))

            RatingSection(product.Rating)

            Spacer(Modifier.height(10.dp))
        }
    }
}

@Composable
private fun ProductImage(imageUrl: String?, modifier: Modifier) {
    SubcomposeAsyncImage(
        model = ImageRequest.Builder(LocalContext.current)
            .data(imageUrl)
            .crossfade(true)
            .setHeader("User-Agent", "Mozilla/5.0")
            .build(),
        contentDescription = "Product Image",
        contentScale = ContentScale.Crop,
        modifier = modifier
    ) {
        when (painter.state) {
            is AsyncImagePainter.State.Loading -> ShimmerBox(Modifier
                .fillMaxSize()
                .clip(RoundedCornerShape(8.dp)))
            is AsyncImagePainter.State.Error -> Image(painterResource(R.drawable.ic_placeholder), "Error", contentScale = ContentScale.Crop, modifier = Modifier.fillMaxSize())
            else -> SubcomposeAsyncImageContent()
        }
    }
}

@Composable
private fun PriceSection(price: Double, initialPrice: Double, currency: String, usdToInrRate: Double) {
    Column(verticalArrangement = Arrangement.spacedBy(2.dp)) {
        val currencySymbol = if (currency == "USD") "$" else "₹"
        val convertedPrice = price.convertPrice(currency, usdToInrRate)
        val convertedInitialPrice = initialPrice.convertPrice(currency, usdToInrRate)

        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            Text("$currencySymbol${"%.2f".format(convertedPrice)}", fontSize = 16.sp, fontWeight = FontWeight.Bold, color = Color.Black)
            if (initialPrice.toInt() != 0) {
                Text("$currencySymbol${"%.2f".format(convertedInitialPrice)}", fontSize = 12.sp, color = Color.Gray, textDecoration = TextDecoration.LineThrough)
            }
        }

        if (initialPrice.toInt() != 0) {
            val discountPercentage = ((initialPrice - price) / price * 100).toInt()
            Text("${discountPercentage}% OFF", fontSize = 10.sp, color = Color(0xFF4CAF50), fontWeight = FontWeight.Bold)
        }
    }
}


@Composable
private fun RatingSection(rating: Double) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(4.dp)
    ) {
        Text(rating.toString(), fontSize = 12.sp, fontWeight = FontWeight.Medium, color = Color.Black)
        Row {
            repeat(5) { index ->
                Icon(
                    if (index < rating.toInt()) Icons.Filled.Star else Icons.Default.StarBorder,
                    "Star",
                    tint = Color(0xFFFFC107),
                    modifier = Modifier.size(12.dp)
                )
            }
        }
    }
}

@Composable
private fun Pagination(
    currentPage: Int,
    totalPages: Int,
    onPageChange: (Int) -> Unit,
    isSorted: Boolean,
    sortField: String,
    isLoading: Boolean
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .background(Color.White)
            .padding(horizontal = 16.dp, vertical = 8.dp)
    ) {
        if (isLoading) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.Center,
                verticalAlignment = Alignment.CenterVertically
            ) {
                CircularProgressIndicator(Modifier.size(20.dp), strokeWidth = 2.dp, color = colorResource(R.color.primaryColor))
                Spacer(Modifier.width(8.dp))
                Text("Loading page $currentPage...", fontSize = 12.sp, color = Color.Gray)
            }
            Spacer(Modifier.height(8.dp))
        }

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Button(
                onClick = { onPageChange(currentPage - 1) },
                enabled = currentPage > 1 && !isLoading,
                colors = ButtonDefaults.buttonColors(colorResource(R.color.primaryColor), disabledContainerColor = Color.Gray.copy(0.3f)),
                shape = RoundedCornerShape(8.dp)
            ) {
                Icon(Icons.AutoMirrored.Filled.ArrowBack, "Previous", modifier = Modifier.size(16.dp))
            }

            Column(horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.spacedBy(4.dp)) {
                PageNumbers(currentPage, totalPages, onPageChange, isLoading)

                Column(horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.spacedBy(2.dp)) {
                    if (isSorted) {
                        Text("Sorted by $sortField", fontSize = 10.sp, color = colorResource(R.color.primaryColor), fontWeight = FontWeight.Medium)
                    }
                    Text("Page $currentPage of $totalPages", fontSize = 10.sp, color = Color.Gray)
                }
            }

            Button(
                onClick = { onPageChange(currentPage + 1) },
                enabled = currentPage < totalPages && !isLoading,
                colors = ButtonDefaults.buttonColors(colorResource(R.color.primaryColor), disabledContainerColor = Color.Gray.copy(0.3f)),
                shape = RoundedCornerShape(8.dp)
            ) {
                Icon(Icons.AutoMirrored.Filled.ArrowForward, "Next", modifier = Modifier.size(16.dp))
            }
        }
    }
}

@Composable
private fun PageNumbers(currentPage: Int, totalPages: Int, onPageChange: (Int) -> Unit, isLoading: Boolean) {
    Row(horizontalArrangement = Arrangement.spacedBy(8.dp), verticalAlignment = Alignment.CenterVertically) {
        val startPage = maxOf(1, currentPage - 1)
        val endPage = minOf(totalPages, startPage + 3)

        if (startPage > 1) {
            PageButton(1, currentPage == 1, { onPageChange(1) }, isLoading)
            if (startPage > 2) Text("...", color = Color.Gray)
        }

        for (page in startPage..endPage) {
            PageButton(page, page == currentPage, { onPageChange(page) }, isLoading)
        }

        if (endPage < totalPages) {
            if (endPage < totalPages - 1) Text("...", color = Color.Gray)
            PageButton(totalPages, currentPage == totalPages, { onPageChange(totalPages) }, isLoading)
        }
    }
}

@Composable
private fun PageButton(pageNumber: Int, isSelected: Boolean, onClick: () -> Unit, isLoading: Boolean) {
    Text(
        pageNumber.toString(),
        fontSize = 14.sp,
        modifier = Modifier
            .clickable(enabled = !isLoading) { if (!isLoading) onClick() }
            .padding(4.dp),
        color = when {
            !isLoading -> Color.Gray.copy(0.5f)
            isSelected -> colorResource(R.color.primaryColor)
            else -> Color.LightGray
        },
        fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal
    )
}

@Composable
private fun LoadingView() {
    Box(Modifier
        .fillMaxWidth()
        .height(200.dp), contentAlignment = Alignment.Center) {
        CircularProgressIndicator(color = colorResource(R.color.primaryColor))
    }
}

@Composable
private fun EmptyState(state: ProductListState, searchQuery: String, onRefresh: () -> Unit) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(32.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Icon(Icons.Default.Search, "No products", modifier = Modifier.size(64.dp), tint = Color.Gray)
        Spacer(Modifier.height(16.dp))

        Text(
            getEmptyStateTitle(state, searchQuery),
            fontSize = 16.sp,
            fontWeight = FontWeight.Medium,
            color = Color.Black,
            textAlign = TextAlign.Center
        )

        Spacer(Modifier.height(8.dp))

        Text(
            getEmptyStateSubtitle(state, searchQuery),
            fontSize = 14.sp,
            color = Color.Gray,
            textAlign = TextAlign.Center
        )

        Spacer(Modifier.height(24.dp))

        Button(
            onClick = onRefresh,
            colors = ButtonDefaults.buttonColors(colorResource(R.color.primaryColor)),
            shape = RoundedCornerShape(8.dp)
        ) {
            Text(getEmptyStateButtonText(state, searchQuery), color = Color.White, fontWeight = FontWeight.Medium)
        }
    }
}

@Composable
private fun ErrorOverlay(error: String, onRetry: () -> Unit) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(16.dp),
        colors = CardDefaults.cardColors(Color.Red.copy(0.1f))
    ) {
        Column(Modifier.padding(16.dp), horizontalAlignment = Alignment.CenterHorizontally) {
            Text(error, color = Color.Red, fontSize = 14.sp, textAlign = TextAlign.Center)
            Spacer(Modifier.height(8.dp))
            Button(onClick = onRetry, colors = ButtonDefaults.buttonColors(Color.Red)) {
                Text("Retry", color = Color.White)
            }
        }
    }
}

// Helper functions
private fun getSectionTitle(state: ProductListState): String = when {
    state.searchQuery.isNotEmpty() -> "Search Results"
    state.selectedCategory != null -> "${state.selectedCategory} Products"
    state.isSorted && state.currentPage > 1 -> "${state.currentSortField} Sorted - Page ${state.currentPage}"
    state.isSorted -> "${state.currentSortField} Sorted Products"
    state.currentPage > 1 -> "All Products - Page ${state.currentPage}"
    else -> "All Products"
}

private fun getActionText(state: ProductListState): String = when {
    state.searchQuery.isNotEmpty() -> "Clear Search"
    state.isSorted -> "Clear Sort"
    else -> "View All"
}

private fun getRefreshAction(state: ProductListState, searchQuery: String): ProductListAction = when {
    state.searchQuery.isNotEmpty() -> ProductListAction.ViewAllProducts
    state.isSorted -> ProductListAction.SortBy("Clear Sort")
    else -> ProductListAction.RefreshProducts
}

private fun getEmptyStateTitle(state: ProductListState, searchQuery: String): String = when {
    searchQuery.isNotBlank() -> {
        val indexQuery = searchQuery.trim().toIntOrNull()
        if (indexQuery != null) "No product found at index $searchQuery"
        else "No products found for '$searchQuery'"
    }
    state.isSorted -> "No products found when sorted by ${state.currentSortField}"
    state.selectedCategory != null -> "No products found in ${state.selectedCategory}"
    else -> "No products available"
}

private fun getEmptyStateSubtitle(state: ProductListState, searchQuery: String): String = when {
    searchQuery.isNotBlank() -> {
        val indexQuery = searchQuery.trim().toIntOrNull()
        if (indexQuery != null) "Try searching for a different index number"
        else "Try a different search term or browse all products"
    }
    state.isSorted -> "Try a different sort option or clear the sort"
    state.selectedCategory != null -> "Try selecting a different category"
    else -> "Pull to refresh or try again later"
}

private fun getEmptyStateButtonText(state: ProductListState, searchQuery: String): String = when {
    searchQuery.isNotBlank() -> "Clear Search"
    state.isSorted -> "Clear Sort"
    else -> "Refresh"
}