package com.example.azureaadmin.utils


import com.example.azureaadmin.data.models.MonthlyReportResponse

fun generateReportHTML(report: MonthlyReportResponse): String {
    val occupancyRate = if (report.stats.totalRooms > 0) {
        String.format("%.1f%%", (report.stats.occupiedRooms.toFloat() / report.stats.totalRooms) * 100)
    } else "0%"

    // Calculate totals for summary
    val totalRoomRevenue = report.roomRevenueValues.sum()
    val totalAreaRevenue = report.areaRevenueValues.sum()
    val totalRoomBookings = report.roomBookingValues.sum()
    val totalAreaBookings = report.areaBookingValues.sum()

    return """
<!DOCTYPE html>
<html>
<head>
    <meta name="viewport" content="width=device-width, initial-scale=1.0">

    <!-- Roboto Font -->
    <link href="https://fonts.googleapis.com/css2?family=Roboto:wght@300;400;500;700&display=swap" rel="stylesheet">

    <style>
        * {
            margin: 0;
            padding: 0;
            box-sizing: border-box;
        }

        body {
            font-family: 'Roboto', sans-serif;
            color: #2d3748;
            background: white;
            padding: 25px 25px;
            line-height: 1.55;
        }

        .container {
            max-width: 820px;
            margin: 0 auto;
        }

        .header {
            text-align: center;
            margin-bottom: 30px;
            padding-bottom: 20px;
            border-bottom: 3px solid #2b6cb0;
        }

        .header h1 {
            font-size: 26px;
            color: #2b6cb0;
            font-weight: 700;
            margin-bottom: 8px;
        }

        .header h2 {
            font-size: 18px;
            font-weight: 500;
            color: #4a5568;
            margin-bottom: 6px;
        }

        .header h3 {
            font-size: 13px;
            color: #718096;
            font-weight: 400;
        }

        .section {
            margin-bottom: 10px;
            padding: 5px 2px;
            page-break-inside: avoid;
        }

        .section-title {
            font-size: 17px;
            font-weight: 600;
            margin-bottom: 15px;
            color: #2b6cb0;
            border-bottom: 2px solid #e2e8f0;
            padding-bottom: 6px;
        }

        .description {
            font-size: 14px;
            color: #4a5568;
            margin-bottom: 16px;
        }

        .kpi-grid {
            display: grid;
            grid-template-columns: repeat(auto-fit, minmax(140px, 1fr));
            gap: 15px;
            margin-top: 8px;
        }

        .kpi-card {
            background: #f7fafc;
            border: 1px solid #e2e8f0;
            border-radius: 8px;
            padding: 16px;
            text-align: center;
        }

        .kpi-label {
            font-size: 11px;
            text-transform: uppercase;
            color: #718096;
            margin-bottom: 6px;
            font-weight: 500;
        }

        .kpi-value {
            font-size: 20px;
            font-weight: 700;
            color: #2b6cb0;
        }

        .status-item {
            display: flex;
            align-items: center;
            padding: 12px 5px;
            border-bottom: 1px solid #e2e8f0;
        }

        .status-color {
            width: 12px;
            height: 12px;
            border-radius: 2px;
            margin-right: 12px;
        }

        .status-label {
            flex: 1;
            display: flex;
            justify-content: space-between;
            font-size: 14px;
        }

        .status-value {
            font-weight: 600;
        }

        table {
        width: 100%;
        border-collapse: collapse;
        margin-top: 6px;
        }
        
        thead {
            background: #e2e8f0;
            border-bottom: none; /* no bottom line */
        }
        
        th {
            padding: 6px 6px;
            text-align: left;         /* left align header */
            font-size: 12px;
            font-weight: 600;
            border-bottom: none !important; /* remove header line */
        }
        
        td {
            padding: 6px 6px;
            text-align: left;         /* left align body */
            font-size: 12px;
            border-bottom: 1px solid #e2e8f0;
            line-height: 1.25;
        }
        
        tbody tr:nth-child(even) {
            background: #f7fafc;
        }


        .summary-box {
            background: #edf2f7;
            padding: 14px;
            border-left: 4px solid #2b6cb0;
            border-radius: 4px;
            margin-top: 12px;
        }

        .summary-row {
            display: flex;
            justify-content: space-between;
            font-size: 14px;
            padding: 6px 0;
        }

        .summary-label {
            font-weight: 600;
            color: #4a5568;
        }

        .summary-value {
            font-weight: 700;
            color: #2b6cb0;
        }

        .no-data {
            padding: 20px;
            font-size: 14px;
            text-align: center;
            color: #718096;
            font-style: italic;
        }
        
        @page {
            margin: 25px 25px;
            
            @bottom-left {
                content: "Azurea Hotel – ${report.period} Report";
                font-size: 10px;
                color: #718096;
            }

            @bottom-right {
                content: "Page " counter(page) " of " counter(pages);
                font-size: 10px;
                color: #718096;
            }
        }


        @media print {
    body {
        padding: 25px 25px !important;
        margin: 0;
    }

    .section {
        page-break-inside: avoid;   
        padding-bottom: 10px;
    }

    .header {
        page-break-after: avoid;
        padding-bottom: 10px;
    }

    table, tr, td, th {
        page-break-inside: avoid;
    }

    html, body {
        height: auto;
        overflow: visible;
    }
}



    </style>
</head>
<body>
    <div class="container">
        <div class="header">
            <h1>Azurea Hotel Management System</h1>
            <h2>Monthly Performance Report - ${report.period}</h2>
            <h3>Generated: ${java.text.SimpleDateFormat("MMM dd, yyyy 'at' h:mm a", java.util.Locale.getDefault()).format(java.util.Date())}</h3>
        </div>
        
        <div class="section">
            <div class="section-title">Executive Summary</div>
            <p class="description">
                This monthly performance report provides a comprehensive overview of hotel operations for ${report.period}. 
                The hotel recorded ${report.stats.totalBookings} total bookings with ${report.stats.formattedRevenue} in revenue.
                The current occupancy rate is $occupancyRate with ${report.stats.occupiedRooms} rooms occupied out of ${report.stats.totalRooms} total rooms.
            </p>
        </div>
        
        <div class="section">
            <div class="section-title">Key Performance Indicators</div>
            <div class="kpi-grid">
                <div class="kpi-card">
                    <div class="kpi-label">Active Bookings</div>
                    <div class="kpi-value">${report.stats.activeBookings}</div>
                </div>
                <div class="kpi-card">
                    <div class="kpi-label">Pending Bookings</div>
                    <div class="kpi-value">${report.stats.pendingBookings}</div>
                </div>
                <div class="kpi-card">
                    <div class="kpi-label">Total Monthly Bookings</div>
                    <div class="kpi-value">${report.stats.totalBookings}</div>
                </div>
                <div class="kpi-card">
                    <div class="kpi-label">Monthly Revenue</div>
                    <div class="kpi-value">${report.stats.formattedRevenue}</div>
                </div>
               
            </div>
        </div>
        
        <div class="section">
            <div class="section-title">Booking Status Distribution</div>
            <div class="status-item">
                <div class="status-color" style="background: #4299e1;"></div>
                <div class="status-label">
                    <span>Reserved</span>
                    <span class="status-value">${report.bookingStatusCounts.reserved}</span>
                </div>
            </div>
            <div class="status-item">
                <div class="status-color" style="background: #48bb78;"></div>
                <div class="status-label">
                    <span>Checked Out</span>
                    <span class="status-value">${report.bookingStatusCounts.checked_out}</span>
                </div>
            </div>
            <div class="status-item">
                <div class="status-color" style="background: #f56565;"></div>
                <div class="status-label">
                    <span>Cancelled</span>
                    <span class="status-value">${report.bookingStatusCounts.cancelled}</span>
                </div>
            </div>
            <div class="status-item">
                <div class="status-color" style="background: #ed8936;"></div>
                <div class="status-label">
                    <span>No Show</span>
                    <span class="status-value">${report.bookingStatusCounts.no_show}</span>
                </div>
            </div>
            <div class="status-item">
                <div class="status-color" style="background: #9f7aea;"></div>
                <div class="status-label">
                    <span>Rejected</span>
                    <span class="status-value">${report.bookingStatusCounts.rejected}</span>
                </div>
            </div>
        </div>
        
        <div class="section">
        <div class="section-title">Room Revenue Breakdown</div>
        ${if (totalRoomBookings > 0 || totalRoomRevenue > 0) """
        <table>
            <thead>
                <tr>
                    <th>Room Type</th>
                    <th>Bookings</th>
                    <th>Revenue</th>
                </tr>
            </thead>
            <tbody>
                ${report.roomNames.mapIndexed { index, name ->
            val bookings = report.roomBookingValues.getOrNull(index) ?: 0
            val revenue = report.roomRevenueValues.getOrNull(index) ?: 0.0
            """
                    <tr>
                        <td>$name</td>
                        <td class="text-right">$bookings</td>
                        <td class="text-right">₱${String.format("%,.2f", revenue)}</td>
                    </tr>
                    """
        }.joinToString("")}
            </tbody>
        </table>
        """ else """
        <div class="no-data">No room booking data available for this period</div>
        """}
    </div>
    
            
        <div class="section">
        <div class="section-title">Area Revenue Breakdown</div>
        ${if (totalAreaBookings > 0 || totalAreaRevenue > 0) """
        <table>
            <thead>
                <tr>
                    <th>Area</th>
                    <th>Bookings</th>
                    <th>Revenue</th>
                </tr>
            </thead>
            <tbody>
                ${report.areaNames.mapIndexed { index, name ->
            val bookings = report.areaBookingValues.getOrNull(index) ?: 0
            val revenue = report.areaRevenueValues.getOrNull(index) ?: 0.0
            """
                    <tr>
                        <td>$name</td>
                        <td class="text-right">$bookings</td>
                        <td class="text-right">₱${String.format("%,.2f", revenue)}</td>
                    </tr>
                    """
        }.joinToString("")}
            </tbody>
        </table>
        """ else """
        <div class="no-data">No venue booking data available for this period</div>
        """}
    </div>

        
        <div class="section">
            <div class="section-title">Revenue Summary</div>
            <div class="summary-box">
                <div class="summary-row">
                    <span class="summary-label">Room Revenue:</span>
                    <span class="summary-value">₱${String.format("%,.2f", report.stats.roomRevenue)}</span>
                </div>
                <div class="summary-row">
                    <span class="summary-label">Area Revenue:</span>
                    <span class="summary-value">₱${String.format("%,.2f", report.stats.venueRevenue)}</span>
                </div>
                <div class="summary-row" style="border-top: 2px solid #2b6cb0; margin-top: 10px; padding-top: 10px;">
                    <span class="summary-label">Total Revenue:</span>
                    <span class="summary-value">${report.stats.formattedRevenue}</span>
                </div>
            </div>
        </div>
        
        <div class="section">
            <p class="description">
                This report summarizes the hotel's performance for ${report.period}. The total revenue was 
                ${report.stats.formattedRevenue}, with room bookings contributing ₱${String.format("%,.2f", report.stats.roomRevenue)} 
                (${String.format("%.1f%%", if (report.stats.revenue > 0) (report.stats.roomRevenue / report.stats.revenue) * 100 else 0.0)}) 
                and venue bookings contributing ₱${String.format("%,.2f", report.stats.venueRevenue)} 
                (${String.format("%.1f%%", if (report.stats.revenue > 0) (report.stats.venueRevenue / report.stats.revenue) * 100 else 0.0)}). 
                There were ${report.stats.totalBookings} total bookings, with ${report.stats.activeBookings} currently active 
                and ${report.stats.pendingBookings} pending approval. The hotel maintained an occupancy rate of $occupancyRate 
                with ${report.stats.maintenanceRooms} rooms under maintenance.
            </p>
        </div>
    </div>
</body>
</html>
    """.trimIndent()
}