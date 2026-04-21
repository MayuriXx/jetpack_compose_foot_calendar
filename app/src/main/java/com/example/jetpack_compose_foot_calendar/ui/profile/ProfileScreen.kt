package com.example.jetpack_compose_foot_calendar.ui.profile

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import coil.compose.AsyncImage
import com.example.jetpack_compose_foot_calendar.domain.model.Team
import com.example.jetpack_compose_foot_calendar.ui.common.UiState

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ProfileScreen(
    viewModel: ProfileViewModel,
    authViewModel: com.example.jetpack_compose_foot_calendar.ui.auth.AuthViewModel,
    onBack: () -> Unit,
    onLogout: () -> Unit
) {
    val favoriteTeam     by viewModel.favoriteTeam.collectAsState()
    val user             by authViewModel.user.collectAsState()
    val selectedLeagueId by viewModel.selectedLeagueId.collectAsState()
    val teamsState       by viewModel.teamsState.collectAsState()
    val filteredTeams    by viewModel.filteredTeams.collectAsState()
    val searchQuery      by viewModel.searchQuery.collectAsState()

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Profil") },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, "Retour")
                    }
                }
            )
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(16.dp)
                .verticalScroll(rememberScrollState()),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // ── Infos utilisateur ─────────────────────────────────────────────
            Card(modifier = Modifier.fillMaxWidth()) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text("👤 Utilisateur", style = MaterialTheme.typography.titleSmall)
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(user?.username ?: "Inconnu", style = MaterialTheme.typography.bodyLarge)
                    user?.email?.let { Text(it, style = MaterialTheme.typography.bodySmall) }
                }
            }

            // ── Section équipe favorite ───────────────────────────────────────
            Card(modifier = Modifier.fillMaxWidth()) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text(
                        "⚽ Mon Équipe Favorite",
                        style = MaterialTheme.typography.titleMedium
                    )
                    HorizontalDivider(modifier = Modifier.padding(vertical = 8.dp))

                    // Équipe sélectionnée (ou placeholder)
                    if (favoriteTeam != null) {
                        FavoriteTeamCard(
                            team = favoriteTeam!!,
                            onClear = { viewModel.clearFavoriteTeam() }
                        )
                    } else {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 8.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                "Sélectionnez votre équipe favorite",
                                style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    // ── Sélecteur de ligue ────────────────────────────────────
                    Text("Choisir une Ligue :", style = MaterialTheme.typography.titleSmall)
                    Spacer(modifier = Modifier.height(8.dp))
                    LeagueSelector(
                        leagues     = viewModel.popularLeagues,
                        selectedId  = selectedLeagueId,
                        onSelect    = { viewModel.selectLeague(it) }
                    )

                    Spacer(modifier = Modifier.height(12.dp))

                    // ── Champ de recherche ────────────────────────────────────
                    OutlinedTextField(
                        value         = searchQuery,
                        onValueChange = { viewModel.setSearchQuery(it) },
                        placeholder   = { Text("Chercher une équipe...") },
                        singleLine    = true,
                        modifier      = Modifier.fillMaxWidth()
                    )

                    Spacer(modifier = Modifier.height(12.dp))

                    // ── Grille des équipes ────────────────────────────────────
                    when (teamsState) {
                        is UiState.Loading -> {
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(200.dp),
                                contentAlignment = Alignment.Center
                            ) {
                                CircularProgressIndicator()
                            }
                        }
                        is UiState.Error -> {
                            Text(
                                "⚠️ ${(teamsState as UiState.Error).message}",
                                color = MaterialTheme.colorScheme.error,
                                modifier = Modifier.padding(vertical = 8.dp)
                            )
                        }
                        is UiState.Success -> {
                            if (filteredTeams.isEmpty()) {
                                Text(
                                    "Aucune équipe trouvée",
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(vertical = 24.dp),
                                    textAlign = TextAlign.Center,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            } else {
                                TeamsGrid(
                                    teams        = filteredTeams,
                                    favoriteTeam = favoriteTeam,
                                    onTeamClick  = { viewModel.setFavoriteTeam(it) }
                                )
                            }
                        }
                    }
                }
            }

            // ── Bouton déconnexion ────────────────────────────────────────────
            Button(
                onClick = {
                    authViewModel.logout()
                    onLogout()
                },
                modifier = Modifier.fillMaxWidth(),
                colors = ButtonDefaults.buttonColors(
                    containerColor = MaterialTheme.colorScheme.error
                )
            ) {
                Text("Déconnexion")
            }
        }
    }
}

// ── Composants privés ─────────────────────────────────────────────────────────

@Composable
private fun FavoriteTeamCard(team: Team, onClear: () -> Unit) {
    Card(
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.primaryContainer
        ),
        modifier = Modifier.fillMaxWidth()
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                AsyncImage(
                    model = team.logo,
                    contentDescription = team.name,
                    modifier = Modifier.size(48.dp)
                )
                Spacer(modifier = Modifier.width(12.dp))
                Text(team.name, style = MaterialTheme.typography.titleMedium)
            }
            OutlinedButton(onClick = onClear) {
                Text("✕ Changer")
            }
        }
    }
}

@Composable
private fun LeagueSelector(
    leagues: List<PopularLeague>,
    selectedId: Int,
    onSelect: (Int) -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .horizontalScroll(rememberScrollState()),
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        leagues.forEach { league ->
            val isSelected = league.id == selectedId
            Button(
                onClick = { onSelect(league.id) },
                colors = ButtonDefaults.buttonColors(
                    containerColor = if (isSelected)
                        MaterialTheme.colorScheme.primary
                    else
                        MaterialTheme.colorScheme.surfaceVariant,
                    contentColor = if (isSelected)
                        MaterialTheme.colorScheme.onPrimary
                    else
                        MaterialTheme.colorScheme.onSurfaceVariant
                ),
                contentPadding = PaddingValues(horizontal = 14.dp, vertical = 6.dp)
            ) {
                Text(league.name, style = MaterialTheme.typography.labelMedium)
            }
        }
    }
}

@Composable
private fun TeamsGrid(
    teams: List<Team>,
    favoriteTeam: Team?,
    onTeamClick: (Team) -> Unit
) {
    // LazyVerticalGrid nécessite une hauteur fixe à l'intérieur d'un Column scrollable
    val itemHeight = 130.dp
    val columns = 3
    val rows = (teams.size + columns - 1) / columns
    val gridHeight = itemHeight * rows

    LazyVerticalGrid(
        columns = GridCells.Fixed(columns),
        modifier = Modifier
            .fillMaxWidth()
            .height(gridHeight),
        horizontalArrangement = Arrangement.spacedBy(8.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp),
        userScrollEnabled = false // le scroll est géré par le Column parent
    ) {
        items(teams, key = { it.id }) { team ->
            TeamCard(
                team        = team,
                isSelected  = favoriteTeam?.id == team.id,
                onClick     = { onTeamClick(team) }
            )
        }
    }
}

@Composable
private fun TeamCard(team: Team, isSelected: Boolean, onClick: () -> Unit) {
    val borderColor = if (isSelected)
        MaterialTheme.colorScheme.primary
    else
        MaterialTheme.colorScheme.outlineVariant

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick() },
        border = BorderStroke(
            width = if (isSelected) 2.dp else 1.dp,
            color = borderColor
        ),
        colors = CardDefaults.cardColors(
            containerColor = if (isSelected)
                MaterialTheme.colorScheme.primaryContainer
            else
                MaterialTheme.colorScheme.surface
        )
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(8.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            AsyncImage(
                model = team.logo,
                contentDescription = team.name,
                modifier = Modifier.size(48.dp)
            )
            Spacer(modifier = Modifier.height(6.dp))
            Text(
                text = team.name,
                style = MaterialTheme.typography.labelSmall,
                textAlign = TextAlign.Center,
                maxLines = 2,
                overflow = TextOverflow.Ellipsis
            )
            if (isSelected) {
                Text(
                    "✓",
                    color = MaterialTheme.colorScheme.primary,
                    style = MaterialTheme.typography.labelSmall
                )
            }
        }
    }
}
