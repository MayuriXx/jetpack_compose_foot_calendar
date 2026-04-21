package com.example.jetpack_compose_foot_calendar.ui.matchdetail

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
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
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.Card
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import coil.compose.AsyncImage
import com.example.jetpack_compose_foot_calendar.domain.model.EventType
import com.example.jetpack_compose_foot_calendar.domain.model.Match
import com.example.jetpack_compose_foot_calendar.domain.model.MatchDetail
import com.example.jetpack_compose_foot_calendar.domain.model.MatchEvent
import com.example.jetpack_compose_foot_calendar.domain.model.MatchStatus
import com.example.jetpack_compose_foot_calendar.domain.model.Player
import com.example.jetpack_compose_foot_calendar.domain.model.TeamLineup
import com.example.jetpack_compose_foot_calendar.domain.model.TeamStatistics
import com.example.jetpack_compose_foot_calendar.ui.common.UiState

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MatchDetailScreen(
    fixtureId: Int,
    viewModel: MatchDetailViewModel,
    onBack: () -> Unit
) {
    // Charge les données au premier affichage de l'écran
    // LaunchedEffect(Unit) = onMounted() en Vue — se déclenche une seule fois
    LaunchedEffect(Unit) {
        viewModel.loadMatchDetail(fixtureId)
    }

    val uiState by viewModel.uiState.collectAsState()

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Détail du match") },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Retour")
                    }
                }
            )
        }
    ) { paddingValues ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {
            when (val state = uiState) {
                is UiState.Loading -> {
                    CircularProgressIndicator(modifier = Modifier.align(Alignment.Center))
                }

                is UiState.Error -> {
                    Text(
                        text = "Erreur : ${state.message}",
                        color = MaterialTheme.colorScheme.error,
                        modifier = Modifier.align(Alignment.Center)
                    )
                }

                is UiState.Success -> {
                    MatchDetailContent(detail = state.data)
                }
            }
        }
    }
}

// ── Contenu principal scrollable ──────────────────────────────────────

@Composable
private fun MatchDetailContent(detail: MatchDetail) {
    // LazyColumn car le contenu est long — stats + events + lineups
    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(bottom = 24.dp)
    ) {
        // En-tête du match
        item { MatchHeader(match = detail.match) }

        // Section statistiques
        if (detail.statistics.isNotEmpty()) {
            item { SectionTitle("📊 Statistiques") }
            item { StatisticsSection(statistics = detail.statistics) }
        }

        // Timeline des événements
        if (detail.events.isNotEmpty()) {
            item { SectionTitle("📋 Événements") }
            items(
                items = detail.events,
                key = { "${it.minute}_${it.player}_${it.type}" }
            ) { event ->
                EventItem(event = event)
            }
        }

        // Formations
        if (detail.lineups.isNotEmpty()) {
            item { SectionTitle("👥 Compositions") }
            items(detail.lineups) { lineup ->
                TeamLineupSection(lineup = lineup)
            }
        }
    }
}

// ── MatchHeader ───────────────────────────────────────────────────────

@Composable
private fun MatchHeader(match: Match) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(12.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // Ligue
            Text(
                text = "${match.league.country} · ${match.league.name}",
                style = MaterialTheme.typography.labelMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )

            Spacer(modifier = Modifier.height(16.dp))

            // Équipes + score
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceEvenly,
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Équipe domicile
                TeamBlock(
                    name = match.homeTeam.name,
                    logo = match.homeTeam.logo
                )

                // Score central
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text(
                        text = "${match.score.home ?: "-"}  -  ${match.score.away ?: "-"}",
                        style = MaterialTheme.typography.headlineMedium,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = when (match.status) {
                            MatchStatus.LIVE -> "🔴 EN DIRECT"
                            MatchStatus.FINISHED -> "✅ Terminé"
                            MatchStatus.UPCOMING -> "⏰ À venir"
                            else -> ""
                        },
                        style = MaterialTheme.typography.labelSmall
                    )
                }

                // Équipe extérieure
                TeamBlock(
                    name = match.awayTeam.name,
                    logo = match.awayTeam.logo
                )
            }
        }
    }
}

@Composable
private fun TeamBlock(name: String, logo: String) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        AsyncImage(
            model = logo,
            contentDescription = name,
            modifier = Modifier.size(48.dp)
        )
        Spacer(modifier = Modifier.height(4.dp))
        Text(
            text = name,
            style = MaterialTheme.typography.bodySmall,
            maxLines = 2
        )
    }
}

// ── StatisticsSection ─────────────────────────────────────────────────

@Composable
private fun StatisticsSection(statistics: List<TeamStatistics>) {
    if (statistics.size < 2) return

    val homeStats = statistics[0]
    val awayStats = statistics[1]
    // Clés communes aux deux équipes
    val statKeys = homeStats.stats.keys.intersect(awayStats.stats.keys)

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 12.dp, vertical = 4.dp)
    ) {
        Column(modifier = Modifier.padding(12.dp)) {
            statKeys.forEach { key ->
                StatRow(
                    label = key,
                    homeValue = homeStats.stats[key] ?: "-",
                    awayValue = awayStats.stats[key] ?: "-"
                )
                Spacer(modifier = Modifier.height(4.dp))
            }
        }
    }
}

@Composable
private fun StatRow(label: String, homeValue: String, awayValue: String) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = homeValue,
            style = MaterialTheme.typography.bodyMedium,
            fontWeight = FontWeight.Bold,
            modifier = Modifier.weight(1f)
        )
        Text(
            text = label,
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.weight(2f),
            textAlign = androidx.compose.ui.text.style.TextAlign.Center
        )
        Text(
            text = awayValue,
            style = MaterialTheme.typography.bodyMedium,
            fontWeight = FontWeight.Bold,
            modifier = Modifier.weight(1f),
            textAlign = androidx.compose.ui.text.style.TextAlign.End
        )
    }
}

// ── EventItem ─────────────────────────────────────────────────────────

@Composable
private fun EventItem(event: MatchEvent) {
    val icon = when (event.type) {
        EventType.GOAL -> "⚽"
        EventType.CARD -> "🟨"
        EventType.SUBSTITUTION -> "🔄"
        EventType.VAR -> "📺"
        else -> "•"
    }

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 12.dp, vertical = 4.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = "${event.minute}'",
            style = MaterialTheme.typography.labelMedium,
            modifier = Modifier.width(36.dp)
        )
        Text(text = icon, modifier = Modifier.width(24.dp))
        Column {
            Text(
                text = event.player,
                style = MaterialTheme.typography.bodyMedium
            )
            Text(
                text = event.detail,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
        Spacer(modifier = Modifier.weight(1f))
        AsyncImage(
            model = event.team.logo,
            contentDescription = event.team.name,
            modifier = Modifier.size(20.dp)
        )
    }
}

// ── TeamLineupSection ─────────────────────────────────────────────────

@Composable
private fun TeamLineupSection(lineup: TeamLineup) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 12.dp, vertical = 4.dp)
    ) {
        Column(modifier = Modifier.padding(12.dp)) {
            // En-tête équipe + formation
            Row(verticalAlignment = Alignment.CenterVertically) {
                AsyncImage(
                    model = lineup.team.logo,
                    contentDescription = lineup.team.name,
                    modifier = Modifier.size(28.dp)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = lineup.team.name,
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.Bold
                )
                Spacer(modifier = Modifier.weight(1f))
                Text(
                    text = lineup.formation,
                    style = MaterialTheme.typography.labelMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            Spacer(modifier = Modifier.height(8.dp))

            // Titulaires
            Text("Titulaires", style = MaterialTheme.typography.labelMedium)
            lineup.startXI.forEach { player ->
                PlayerRow(player = player)
            }

            Spacer(modifier = Modifier.height(8.dp))

            // Remplaçants
            Text("Remplaçants", style = MaterialTheme.typography.labelMedium)
            lineup.substitutes.forEach { player ->
                PlayerRow(player = player)
            }
        }
    }
}

@Composable
private fun PlayerRow(player: Player) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 2.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = "${player.number}",
            style = MaterialTheme.typography.labelMedium,
            modifier = Modifier.width(28.dp)
        )
        Text(
            text = player.name,
            style = MaterialTheme.typography.bodySmall
        )
        Spacer(modifier = Modifier.weight(1f))
        Text(
            text = player.position,
            style = MaterialTheme.typography.labelSmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}

// ── Utilitaire ────────────────────────────────────────────────────────

@Composable
private fun SectionTitle(title: String) {
    Text(
        text = title,
        style = MaterialTheme.typography.titleMedium,
        fontWeight = FontWeight.Bold,
        modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp)
    )
}