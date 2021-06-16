namespace ProfilerClient
{
	partial class Form1
	{
		/// <summary>
		/// Required designer variable.
		/// </summary>
		private System.ComponentModel.IContainer components = null;

		/// <summary>
		/// Clean up any resources being used.
		/// </summary>
		/// <param name="disposing">true if managed resources should be disposed; otherwise, false.</param>
		protected override void Dispose(bool disposing)
		{
			if (disposing && (components != null))
			{
				components.Dispose();
			}
			base.Dispose(disposing);
		}

		#region Windows Form Designer generated code

		/// <summary>
		/// Required method for Designer support - do not modify
		/// the contents of this method with the code editor.
		/// </summary>
		private void InitializeComponent()
		{
			this.components = new System.ComponentModel.Container();
			this.button1 = new System.Windows.Forms.Button();
			this.timer1 = new System.Windows.Forms.Timer(this.components);
			this.backgroundWorker1 = new System.ComponentModel.BackgroundWorker();
			this.textBox1 = new System.Windows.Forms.TextBox();
			this.treeViewAdv1 = new Aga.Controls.Tree.TreeViewAdv();
			this.treeColumn1 = new Aga.Controls.Tree.TreeColumn();
			this.treeColumn2 = new Aga.Controls.Tree.TreeColumn();
			this.treeColumn3 = new Aga.Controls.Tree.TreeColumn();
			this.treeColumn4 = new Aga.Controls.Tree.TreeColumn();
			this.treeColumn5 = new Aga.Controls.Tree.TreeColumn();
			this.treeColumn6 = new Aga.Controls.Tree.TreeColumn();
			this.treeColumn7 = new Aga.Controls.Tree.TreeColumn();
			this.nodeTextBox1 = new Aga.Controls.Tree.NodeControls.NodeTextBox();
			this.nodeDecimalTextBox1 = new Aga.Controls.Tree.NodeControls.NodeDecimalTextBox();
			this.nodeDecimalTextBox2 = new Aga.Controls.Tree.NodeControls.NodeDecimalTextBox();
			this.nodeDecimalTextBox3 = new Aga.Controls.Tree.NodeControls.NodeDecimalTextBox();
			this.nodeDecimalTextBox4 = new Aga.Controls.Tree.NodeControls.NodeDecimalTextBox();
			this.nodeDecimalTextBox5 = new Aga.Controls.Tree.NodeControls.NodeDecimalTextBox();
			this.nodeDecimalTextBox6 = new Aga.Controls.Tree.NodeControls.NodeDecimalTextBox();
			this.panel1 = new System.Windows.Forms.Panel();
			this.panel1.SuspendLayout();
			this.SuspendLayout();
			// 
			// button1
			// 
			this.button1.AutoSize = true;
			this.button1.AutoSizeMode = System.Windows.Forms.AutoSizeMode.GrowAndShrink;
			this.button1.Dock = System.Windows.Forms.DockStyle.Right;
			this.button1.Location = new System.Drawing.Point(612, 0);
			this.button1.Name = "button1";
			this.button1.Size = new System.Drawing.Size(57, 25);
			this.button1.TabIndex = 0;
			this.button1.Text = "Connect";
			this.button1.UseVisualStyleBackColor = true;
			this.button1.Click += new System.EventHandler(this.button1_Click);
			// 
			// timer1
			// 
			this.timer1.Enabled = true;
			this.timer1.Tick += new System.EventHandler(this.timer1_Tick);
			// 
			// backgroundWorker1
			// 
			this.backgroundWorker1.WorkerSupportsCancellation = true;
			this.backgroundWorker1.DoWork += new System.ComponentModel.DoWorkEventHandler(this.backgroundWorker1_DoWork);
			this.backgroundWorker1.RunWorkerCompleted += new System.ComponentModel.RunWorkerCompletedEventHandler(this.backgroundWorker1_RunWorkerCompleted);
			// 
			// textBox1
			// 
			this.textBox1.Dock = System.Windows.Forms.DockStyle.Fill;
			this.textBox1.Location = new System.Drawing.Point(0, 0);
			this.textBox1.Name = "textBox1";
			this.textBox1.Size = new System.Drawing.Size(612, 20);
			this.textBox1.TabIndex = 1;
			this.textBox1.Text = "localhost:5000";
			this.textBox1.WordWrap = false;
			// 
			// treeViewAdv1
			// 
			this.treeViewAdv1.AllowColumnReorder = true;
			this.treeViewAdv1.BackColor = System.Drawing.SystemColors.Window;
			this.treeViewAdv1.Columns.Add(this.treeColumn1);
			this.treeViewAdv1.Columns.Add(this.treeColumn2);
			this.treeViewAdv1.Columns.Add(this.treeColumn3);
			this.treeViewAdv1.Columns.Add(this.treeColumn4);
			this.treeViewAdv1.Columns.Add(this.treeColumn5);
			this.treeViewAdv1.Columns.Add(this.treeColumn6);
			this.treeViewAdv1.Columns.Add(this.treeColumn7);
			this.treeViewAdv1.DefaultToolTipProvider = null;
			this.treeViewAdv1.Dock = System.Windows.Forms.DockStyle.Fill;
			this.treeViewAdv1.DragDropMarkColor = System.Drawing.Color.Black;
			this.treeViewAdv1.FullRowSelect = true;
			this.treeViewAdv1.GridLineStyle = ((Aga.Controls.Tree.GridLineStyle)((Aga.Controls.Tree.GridLineStyle.Horizontal | Aga.Controls.Tree.GridLineStyle.Vertical)));
			this.treeViewAdv1.Indent = 12;
			this.treeViewAdv1.LineColor = System.Drawing.SystemColors.ControlDark;
			this.treeViewAdv1.Location = new System.Drawing.Point(0, 25);
			this.treeViewAdv1.Model = null;
			this.treeViewAdv1.Name = "treeViewAdv1";
			this.treeViewAdv1.NodeControls.Add(this.nodeTextBox1);
			this.treeViewAdv1.NodeControls.Add(this.nodeDecimalTextBox1);
			this.treeViewAdv1.NodeControls.Add(this.nodeDecimalTextBox2);
			this.treeViewAdv1.NodeControls.Add(this.nodeDecimalTextBox3);
			this.treeViewAdv1.NodeControls.Add(this.nodeDecimalTextBox4);
			this.treeViewAdv1.NodeControls.Add(this.nodeDecimalTextBox5);
			this.treeViewAdv1.NodeControls.Add(this.nodeDecimalTextBox6);
			this.treeViewAdv1.SelectedNode = null;
			this.treeViewAdv1.Size = new System.Drawing.Size(669, 344);
			this.treeViewAdv1.TabIndex = 2;
			this.treeViewAdv1.Text = "treeViewAdv1";
			this.treeViewAdv1.UseColumns = true;
			// 
			// treeColumn1
			// 
			this.treeColumn1.Header = "Callstack";
			this.treeColumn1.SortOrder = System.Windows.Forms.SortOrder.None;
			this.treeColumn1.TooltipText = "Function name";
			this.treeColumn1.Width = 300;
			// 
			// treeColumn2
			// 
			this.treeColumn2.Header = "TCount";
			this.treeColumn2.SortOrder = System.Windows.Forms.SortOrder.None;
			this.treeColumn2.TextAlign = System.Windows.Forms.HorizontalAlignment.Right;
			this.treeColumn2.TooltipText = "Total no. of allocation count (including child call)";
			this.treeColumn2.Width = 60;
			// 
			// treeColumn3
			// 
			this.treeColumn3.Header = "SCount";
			this.treeColumn3.SortOrder = System.Windows.Forms.SortOrder.None;
			this.treeColumn3.TextAlign = System.Windows.Forms.HorizontalAlignment.Right;
			this.treeColumn3.TooltipText = "Self no. of allocation count (child call not included)";
			this.treeColumn3.Width = 60;
			// 
			// treeColumn4
			// 
			this.treeColumn4.Header = "TkBytes";
			this.treeColumn4.SortOrder = System.Windows.Forms.SortOrder.None;
			this.treeColumn4.TextAlign = System.Windows.Forms.HorizontalAlignment.Right;
			this.treeColumn4.TooltipText = "Total allocated memory in kilo bytes (including child call)";
			this.treeColumn4.Width = 60;
			// 
			// treeColumn5
			// 
			this.treeColumn5.Header = "SkBytes";
			this.treeColumn5.SortOrder = System.Windows.Forms.SortOrder.None;
			this.treeColumn5.TextAlign = System.Windows.Forms.HorizontalAlignment.Right;
			this.treeColumn5.TooltipText = "Self allocated memory in kilo bytes (child call not included)";
			this.treeColumn5.Width = 60;
			// 
			// treeColumn6
			// 
			this.treeColumn6.Header = "SCount/F";
			this.treeColumn6.SortOrder = System.Windows.Forms.SortOrder.None;
			this.treeColumn6.TextAlign = System.Windows.Forms.HorizontalAlignment.Right;
			this.treeColumn6.TooltipText = "Self no. of allocation count per frame";
			this.treeColumn6.Width = 60;
			// 
			// treeColumn7
			// 
			this.treeColumn7.Header = "Call/F";
			this.treeColumn7.SortOrder = System.Windows.Forms.SortOrder.None;
			this.treeColumn7.TextAlign = System.Windows.Forms.HorizontalAlignment.Right;
			this.treeColumn7.TooltipText = "No. of call per frame";
			this.treeColumn7.Width = 60;
			// 
			// nodeTextBox1
			// 
			this.nodeTextBox1.DataPropertyName = "Name";
			this.nodeTextBox1.EditEnabled = false;
			this.nodeTextBox1.IncrementalSearchEnabled = true;
			this.nodeTextBox1.LeftMargin = 3;
			this.nodeTextBox1.ParentColumn = this.treeColumn1;
			// 
			// nodeDecimalTextBox1
			// 
			this.nodeDecimalTextBox1.DataPropertyName = "TCount";
			this.nodeDecimalTextBox1.EditEnabled = false;
			this.nodeDecimalTextBox1.IncrementalSearchEnabled = true;
			this.nodeDecimalTextBox1.LeftMargin = 3;
			this.nodeDecimalTextBox1.ParentColumn = this.treeColumn2;
			this.nodeDecimalTextBox1.TextAlign = System.Windows.Forms.HorizontalAlignment.Right;
			// 
			// nodeDecimalTextBox2
			// 
			this.nodeDecimalTextBox2.DataPropertyName = "SCount";
			this.nodeDecimalTextBox2.EditEnabled = false;
			this.nodeDecimalTextBox2.IncrementalSearchEnabled = true;
			this.nodeDecimalTextBox2.LeftMargin = 3;
			this.nodeDecimalTextBox2.ParentColumn = this.treeColumn3;
			this.nodeDecimalTextBox2.TextAlign = System.Windows.Forms.HorizontalAlignment.Right;
			// 
			// nodeDecimalTextBox3
			// 
			this.nodeDecimalTextBox3.DataPropertyName = "TkBytes";
			this.nodeDecimalTextBox3.EditEnabled = false;
			this.nodeDecimalTextBox3.IncrementalSearchEnabled = true;
			this.nodeDecimalTextBox3.LeftMargin = 3;
			this.nodeDecimalTextBox3.ParentColumn = this.treeColumn4;
			this.nodeDecimalTextBox3.TextAlign = System.Windows.Forms.HorizontalAlignment.Right;
			// 
			// nodeDecimalTextBox4
			// 
			this.nodeDecimalTextBox4.DataPropertyName = "SkBytes";
			this.nodeDecimalTextBox4.EditEnabled = false;
			this.nodeDecimalTextBox4.IncrementalSearchEnabled = true;
			this.nodeDecimalTextBox4.LeftMargin = 3;
			this.nodeDecimalTextBox4.ParentColumn = this.treeColumn5;
			this.nodeDecimalTextBox4.TextAlign = System.Windows.Forms.HorizontalAlignment.Right;
			// 
			// nodeDecimalTextBox5
			// 
			this.nodeDecimalTextBox5.DataPropertyName = "SCountPerFrame";
			this.nodeDecimalTextBox5.IncrementalSearchEnabled = true;
			this.nodeDecimalTextBox5.LeftMargin = 3;
			this.nodeDecimalTextBox5.ParentColumn = this.treeColumn6;
			this.nodeDecimalTextBox5.TextAlign = System.Windows.Forms.HorizontalAlignment.Right;
			// 
			// nodeDecimalTextBox6
			// 
			this.nodeDecimalTextBox6.DataPropertyName = "CallPerFrame";
			this.nodeDecimalTextBox6.IncrementalSearchEnabled = true;
			this.nodeDecimalTextBox6.LeftMargin = 3;
			this.nodeDecimalTextBox6.ParentColumn = this.treeColumn7;
			this.nodeDecimalTextBox6.TextAlign = System.Windows.Forms.HorizontalAlignment.Right;
			// 
			// panel1
			// 
			this.panel1.Controls.Add(this.textBox1);
			this.panel1.Controls.Add(this.button1);
			this.panel1.Dock = System.Windows.Forms.DockStyle.Top;
			this.panel1.Location = new System.Drawing.Point(0, 0);
			this.panel1.Name = "panel1";
			this.panel1.Size = new System.Drawing.Size(669, 25);
			this.panel1.TabIndex = 3;
			// 
			// Form1
			// 
			this.AutoScaleDimensions = new System.Drawing.SizeF(6F, 13F);
			this.AutoScaleMode = System.Windows.Forms.AutoScaleMode.Font;
			this.ClientSize = new System.Drawing.Size(669, 369);
			this.Controls.Add(this.treeViewAdv1);
			this.Controls.Add(this.panel1);
			this.Name = "Form1";
			this.Text = "Memory profiler client";
			this.panel1.ResumeLayout(false);
			this.panel1.PerformLayout();
			this.ResumeLayout(false);

		}

		#endregion

		private System.Windows.Forms.Button button1;
		private System.Windows.Forms.Timer timer1;
		private System.ComponentModel.BackgroundWorker backgroundWorker1;
		private System.Windows.Forms.TextBox textBox1;
		private Aga.Controls.Tree.TreeViewAdv treeViewAdv1;
		private Aga.Controls.Tree.TreeColumn treeColumn1;
		private Aga.Controls.Tree.TreeColumn treeColumn2;
		private Aga.Controls.Tree.NodeControls.NodeTextBox nodeTextBox1;
		private Aga.Controls.Tree.NodeControls.NodeDecimalTextBox nodeDecimalTextBox1;
		private Aga.Controls.Tree.TreeColumn treeColumn3;
		private Aga.Controls.Tree.NodeControls.NodeDecimalTextBox nodeDecimalTextBox2;
		private Aga.Controls.Tree.TreeColumn treeColumn4;
		private Aga.Controls.Tree.TreeColumn treeColumn5;
		private Aga.Controls.Tree.NodeControls.NodeDecimalTextBox nodeDecimalTextBox3;
		private Aga.Controls.Tree.NodeControls.NodeDecimalTextBox nodeDecimalTextBox4;
		private Aga.Controls.Tree.TreeColumn treeColumn6;
		private Aga.Controls.Tree.TreeColumn treeColumn7;
		private Aga.Controls.Tree.NodeControls.NodeDecimalTextBox nodeDecimalTextBox5;
		private Aga.Controls.Tree.NodeControls.NodeDecimalTextBox nodeDecimalTextBox6;
		private System.Windows.Forms.Panel panel1;
	}
}

