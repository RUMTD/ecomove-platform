-- ═══════════════════════════════════════════════════════════════════════
-- EcoMove — Données initiales pour le développement/démo
-- ═══════════════════════════════════════════════════════════════════════

-- Entreprises partenaires
INSERT INTO enterprises (name, siret, api_key) VALUES
  ('TechCorp SA', '12345678901234', 'api_key_techcorp_abc123'),
  ('GreenBiz SARL', '98765432109876', 'api_key_greenbiz_xyz789')
ON CONFLICT DO NOTHING;

-- Utilisateurs de démonstration
-- Mot de passe : "password123" hashé en BCrypt
INSERT INTO users (email, password_hash, first_name, last_name, role, enterprise_id) VALUES
  ('driver@techcorp.com', '$2a$10$example_hash_driver', 'Jean', 'Martin', 'DRIVER', 1),
  ('passenger@techcorp.com', '$2a$10$example_hash_pass', 'Marie', 'Dupont', 'PASSENGER', 1),
  ('admin@ecomove.io', '$2a$10$example_hash_admin', 'Admin', 'EcoMove', 'ADMIN', NULL)
ON CONFLICT DO NOTHING;

-- Trajets de démonstration
INSERT INTO trips (driver_id, departure_address, arrival_address,
                   departure_time, total_seats, available_seats,
                   co2_per_km_kg, status) VALUES
  (1, 'Paris 15e - Rue de la Convention', 'La Défense - Tour Areva',
   NOW() + INTERVAL '2 hours', 4, 3, 0.12, 'ACTIVE'),
  (1, 'Versailles - Gare RER C', 'Paris 8e - Champs-Élysées',
   NOW() + INTERVAL '3 hours', 3, 2, 0.10, 'ACTIVE')
ON CONFLICT DO NOTHING;
