package com.example.jetpack_compose_foot_calendar.ui.calendar

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Card
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Person
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import coil.compose.AsyncImage
import com.example.jetpack_compose_foot_calendar.domain.model.League
import com.example.jetpack_compose_foot_calendar.domain.model.Match
import com.example.jetpack_compose_foot_calendar.domain.model.MatchStatus
import com.example.jetpack_compose_foot_calendar.ui.common.UiState

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CalendarScreen(
    viewModel: CalendarViewModel,
    onMatchClick: (Int) -> Unit,
    onProfileClick: () -> Unit
) {
    val uiState by viewModel.uiState.collectAsState()
    val matchesByLeague by viewModel.matchesByLeague.collectAsState()
    val countries by viewModel.countries.collectAsState()
    val leagues by viewModel.leagues.collectAsState()
    val selectedStatus by viewModel.selectedStatus.collectAsState()
    val selectedCountry by viewModel.selectedCountry.collectAsState()
    val selectedLeague by viewModel.selectedLeague.collectAsState()

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("⚽ Calendrier") },
                actions = {
                    IconButton(onClick = onProfileClick) {
                        Icon(Icons.Default.Person, contentDescription = "Profil")
                    }
                }
            )
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {
            // ── Barre de filtres ──────────────────────────────────
            FilterBar(
                selectedStatus = selectedStatus,
                selectedCountry = selectedCountry,
                selectedLeague = selectedLeague,
                countries = countries,
                leagues = leagues.map { it.name },
                onStatusChange = { viewModel.setStatusFilter(it) },
                onCountryChange = { viewModel.setCountryFilter(it) },
                onLeagueChange = { viewModel.setLeagueFilter(it) },
                onReset = { viewModel.resetFilters() }
            )

            // ── Contenu principal ─────────────────────────────────
            // Équivalent de ton v-if="isLoading" / v-else-if / v-else
            when (val state = uiState) {
                is UiState.Loading -> {
                    Box(
                        modifier = Modifier.fillMaxSize(),
                        contentAlignment = Alignment.Center
                    ) {
                        CircularProgressIndicator()
                    }
                }

                is UiState.Error -> {
                    Box(
                        modifier = Modifier.fillMaxSize(),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = "Erreur : ${state.message}",
                            color = MaterialTheme.colorScheme.error
                        )
                    }
                }

                is UiState.Success -> {
                    if (matchesByLeague.isEmpty()) {
                        Box(
                            modifier = Modifier.fillMaxSize(),
                            contentAlignment = Alignment.Center
                        ) {
                            Text("Aucun match correspondant aux critères")
                        }
                    } else {
                        // LazyColumn = ton v-for sur les ligues
                        LazyColumn {
                            matchesByLeague.forEach { (league, matches) ->
                                // En-tête de la ligue
                                item(key = "header_${league.id}") {
                                    LeagueHeader(league = league)
                                }
                                // Matchs de la ligue
                                items(
                                    items = matches,
                                    key = { it.fixtureId }
                                ) { match ->
                                    MatchCard(
                                        match = match,
                                        onClick = { onMatchClick(match.fixtureId) }
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}

// ── Composants internes ───────────────────────────────────────────────

@Composable
private fun FilterBar(
    selectedStatus: String,
    selectedCountry: String,
    selectedLeague: String,
    countries: List<String>,
    leagues: List<String>,
    onStatusChange: (String) -> Unit,
    onCountryChange: (String) -> Unit,
    onLeagueChange: (String) -> Unit,
    onReset: () -> Unit
) {
    Column(modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp)) {
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            DropdownSelector(
                label = when (selectedStatus) {
                    "live" -> "🔴 En direct"
                    "upcoming" -> "⏰ À venir"
                    "finished" -> "✅ Terminés"
                    else -> "Tous"
                },
                options = listOf(
                    "all" to "Tous", "live" to "🔴 En direct",
                    "upcoming" to "⏰ À venir", "finished" to "✅ Terminés"
                ),
                onSelect = { onStatusChange(it) },
                modifier = Modifier.weight(1f)
            )
            DropdownSelector(
                label = if (selectedCountry == "all") "Pays" else selectedCountry,
                options = listOf("all" to "Tous les pays") + countries.map { it to it },
                onSelect = { onCountryChange(it) },
                modifier = Modifier.weight(1f)
            )
        }

        Spacer(modifier = Modifier.height(4.dp))

        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            DropdownSelector(
                label = if (selectedLeague == "all") "Championnat" else selectedLeague,
                options = listOf("all" to "Tous") + leagues.map { it to it },
                onSelect = { onLeagueChange(it) },
                modifier = Modifier.weight(1f)
            )
            OutlinedButton(
                onClick = onReset,
                modifier = Modifier.weight(1f)
            ) {
                Text("Réinitialiser")
            }
        }
    }
}

// Composant dropdown réutilisable
@Composable
private fun DropdownSelector(
    label: String,
    options: List<Pair<String, String>>,
    onSelect: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    var expanded by remember { mutableStateOf(false) }

    Box(modifier = modifier) {
        OutlinedButton(
            onClick = { expanded = true },
            modifier = Modifier.fillMaxWidth()
        ) {
            Text(label, maxLines = 1)
        }
        DropdownMenu(
            expanded = expanded,
            onDismissRequest = { expanded = false }
        ) {
            options.forEach { (value, display) ->
                DropdownMenuItem(
                    text = { Text(display) },
                    onClick = {
                        onSelect(value)
                        expanded = false
                    }
                )
            }
        }
    }
}

@Composable
private fun LeagueHeader(league: League) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 12.dp, vertical = 8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        AsyncImage(
            model = league.flag ?: league.logo,
            contentDescription = league.country,
            modifier = Modifier.size(20.dp)
        )
        Spacer(modifier = Modifier.width(8.dp))
        AsyncImage(
            model = league.logo,
            contentDescription = league.name,
            modifier = Modifier.size(20.dp)
        )
        Spacer(modifier = Modifier.width(8.dp))
        Text(
            text = "${league.country} · ${league.name}",
            style = MaterialTheme.typography.titleSmall
        )
    }
}

@Composable
fun MatchCard(
    match: Match,
    onClick: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 12.dp, vertical = 4.dp)
            .clickable { onClick() }
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            // Équipe domicile
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.weight(1f)
            ) {
                AsyncImage(
                    model = match.homeTeam.logo,
                    contentDescription = match.homeTeam.name,
                    modifier = Modifier.size(28.dp)
                )
                Spacer(modifier = Modifier.width(6.dp))
                Text(
                    text = match.homeTeam.name,
                    style = MaterialTheme.typography.bodyMedium,
                    maxLines = 1
                )
            }

            // Score / Statut
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                modifier = Modifier.padding(horizontal = 8.dp)
            ) {
                when (match.status) {
                    MatchStatus.UPCOMING -> Text(
                        text = match.date.substring(11, 16), // heure HH:mm
                        style = MaterialTheme.typography.bodyMedium
                    )

                    else -> Text(
                        text = "${match.score.home ?: "-"} - ${match.score.away ?: "-"}",
                        style = MaterialTheme.typography.titleMedium
                    )
                }
                if (match.status == MatchStatus.LIVE) {
                    Text(
                        text = "LIVE",
                        color = MaterialTheme.colorScheme.error,
                        style = MaterialTheme.typography.labelSmall
                    )
                }
            }

            // Équipe extérieur
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.End,
                modifier = Modifier.weight(1f)
            ) {
                Text(
                    text = match.awayTeam.name,
                    style = MaterialTheme.typography.bodyMedium,
                    maxLines = 1
                )
                Spacer(modifier = Modifier.width(6.dp))
                AsyncImage(
                    model = match.awayTeam.logo,
                    contentDescription = match.awayTeam.name,
                    modifier = Modifier.size(28.dp)
                )
            }
        }
    }
}